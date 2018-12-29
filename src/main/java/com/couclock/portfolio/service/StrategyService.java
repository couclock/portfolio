package com.couclock.portfolio.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.couclock.portfolio.entity.Portfolio;
import com.couclock.portfolio.entity.PortfolioHistory;
import com.couclock.portfolio.entity.PortfolioStatus;
import com.couclock.portfolio.entity.PortfolioStatus.MyCurrentStock;
import com.couclock.portfolio.entity.StockDistribution;
import com.couclock.portfolio.entity.StockHistory;
import com.couclock.portfolio.entity.strategies.AcceleratedMomentumStrategy;

/**
 *
 * @author dany
 *
 */
@Service
public class StrategyService {

	private static final Logger log = LoggerFactory.getLogger(StrategyService.class);

	@Autowired
	private StockHistoryService stockHistoryService;

	public Portfolio acceleratedDualMomentum(Portfolio pf) throws Exception {

		LocalDate currentDate = LocalDate.from(pf.endDate);
		LocalDate targetDate = LocalDate.now();
		PortfolioStatus pfStatus = pf.currentStatus;

		AcceleratedMomentumStrategy strategyParameters = (AcceleratedMomentumStrategy) pf.strategyParameters;

		List<StockDistribution> usStocks = strategyParameters.usStocks;
		List<StockDistribution> exUsStocks = strategyParameters.exUsStocks;
		List<StockDistribution> bondStocks = strategyParameters.bondStocks;

		Map<String, Map<LocalDate, StockHistory>> stock2H = new HashMap<>();
		for (String oneStock : pf.strategyParameters.getStockList()) {
			stock2H.put(oneStock, stockHistoryService.getAllByStockCode_Map(oneStock));
		}

		while (currentDate.isBefore(targetDate)) {

			boolean monthLastDay = currentDate.plusDays(1).getDayOfMonth() == 1;

			// On month last day, calculate momentums
			if (monthLastDay) {

				List<StockDistribution> targetStocks = getToBuyStockOnNextMonthStart(currentDate, stock2H, usStocks,
						exUsStocks, bondStocks);
				targetStocks.forEach(oneTargetStock -> {
					pfStatus.toSell.remove(oneTargetStock.stockCode);
				});
				// We consider if at least a targetStock is in current PF, all targetStocks are
				// in
				if (!targetStocks.isEmpty() && pfStatus.containStock(targetStocks.get(0).stockCode)) {
					;
				} else {
					pfStatus.toBuy.clear();
					pfStatus.toBuy.addAll(targetStocks);
					pfStatus.currentStocks.forEach(oneCurrentStock -> {
						if (!pfStatus.toSell.contains(oneCurrentStock.stockCode)) {
							pfStatus.toSell.add(oneCurrentStock.stockCode);
						}
					});
				}
				log.info("Selected targetStock for next month [" + currentDate + "] : " + targetStocks);

			}

			final LocalDate curDate = currentDate;

			// Try to sell what you should
			if (!monthLastDay && !pfStatus.toSell.isEmpty()) {
				List<String> stockToSellToday = pfStatus.toSell.stream() //
						.filter(oneStockCode -> stock2H.get(oneStockCode).containsKey(curDate)) //
						.collect(Collectors.toList());

				stockToSellToday.forEach(oneStockToSell -> {
					StockHistory oneHistory = stock2H.get(oneStockToSell).get(curDate);
					log.info("toSell stock history : " + oneHistory);
					pfStatus.money += oneHistory.open * pfStatus.getStock(oneStockToSell).count;
					pf.addSellEvent(oneHistory.date, pfStatus.getStock(oneStockToSell).count, oneStockToSell);
					pfStatus.removeStock(oneStockToSell);
				});
				pfStatus.toSell.removeAll(stockToSellToday);
			}

			// Try to buy what you should when all stock to sell are sold
			if (!monthLastDay && !pfStatus.toBuy.isEmpty() && pfStatus.toSell.isEmpty()) {
				List<StockDistribution> stockToBuyToday = pfStatus.toBuy.stream() //
						.filter(oneStockToBuy -> stock2H.get(oneStockToBuy.stockCode).containsKey(curDate)) //
						.collect(Collectors.toList());

				stockToBuyToday.forEach(oneStockToBuy -> {
					StockHistory oneHistory = stock2H.get(oneStockToBuy.stockCode).get(curDate);
					log.info("toBuy stock history : " + oneHistory);

					long count = Math.round(Math.floor((pfStatus.money * oneStockToBuy.percent) / oneHistory.open));
					if (count > 0) {
						pfStatus.addStock(count, oneStockToBuy.stockCode);
						pfStatus.money -= count * oneHistory.open;
						pf.addBuyEvent(curDate, count, oneStockToBuy.stockCode);

					}
				});

				pfStatus.toBuy.removeAll(stockToBuyToday);
			}

			PortfolioHistory todayH = getTodayHistory(curDate, pfStatus, stock2H);
			if (todayH != null) {
				pf.history.add(todayH);
			}

			log.warn("PF status [" + currentDate + "] : " + pfStatus);

			currentDate = currentDate.plusDays(1);
		}

		pf.endDate = pf.history.get(pf.history.size() - 1).date;
		pf.endMoney = pf.history.get(pf.history.size() - 1).value;
		pf.currentStatus = pfStatus;
		log.warn("FINAL pf : " + pf);

		return pf;

	}

	/**
	 * Get stocks to buy considering accelerated momentum strategy
	 *
	 * @param currentDate Last day of current month
	 * @param stock2H
	 * @param usStocks
	 * @param exUsStocks
	 * @param bondStocks
	 * @return
	 * @throws Exception
	 */
	private List<StockDistribution> getToBuyStockOnNextMonthStart(LocalDate currentDate,
			Map<String, Map<LocalDate, StockHistory>> stock2H, List<StockDistribution> usStocks,
			List<StockDistribution> exUsStocks, List<StockDistribution> bondStocks) throws Exception {

		// Get Momentum on exUS stock and on sp500 stock
		double momentumExUS = 0;
		for (StockDistribution oneStock : exUsStocks) {
			momentumExUS += oneStock.percent * (getXMonthPerf(stock2H.get(oneStock.stockCode), currentDate, 1)
					+ getXMonthPerf(stock2H.get(oneStock.stockCode), currentDate, 3)
					+ getXMonthPerf(stock2H.get(oneStock.stockCode), currentDate, 6));
		}
		double momentumUS = 0;
		for (StockDistribution oneStock : usStocks) {
			momentumUS += oneStock.percent * (getXMonthPerf(stock2H.get(oneStock.stockCode), currentDate, 1)
					+ getXMonthPerf(stock2H.get(oneStock.stockCode), currentDate, 3)
					+ getXMonthPerf(stock2H.get(oneStock.stockCode), currentDate, 6));
		}

		// Set targetStock
		List<StockDistribution> targetStock = null;
		if (momentumUS > momentumExUS) {
			if (momentumUS > 0) {
				targetStock = usStocks;
			} else {
				targetStock = bondStocks;
			}
		} else {
			if (momentumExUS > 0) {
				targetStock = exUsStocks;
			} else {
				targetStock = bondStocks;
			}
		}

		return targetStock;
	}

	private PortfolioHistory getTodayHistory(LocalDate curDate, PortfolioStatus pfStatus,
			Map<String, Map<LocalDate, StockHistory>> stock2h) {

		double dayValue = pfStatus.money;

		boolean allFound = true;

		for (MyCurrentStock oneStock : pfStatus.currentStocks) {
			if (stock2h.containsKey(oneStock.stockCode) && stock2h.get(oneStock.stockCode).containsKey(curDate)) {
				dayValue += oneStock.count * stock2h.get(oneStock.stockCode).get(curDate).close;
			} else {
				allFound = false;
			}
		}

		if (allFound) {
			return new PortfolioHistory(curDate, dayValue);
		} else {
			return null;
		}

	}

	/**
	 * Get Perf for last monthCount months using date as start date
	 *
	 * @param allHistories
	 * @param date
	 * @param monthCount
	 * @return
	 * @throws Exception
	 */
	private double getXMonthPerf(Map<LocalDate, StockHistory> allHistories, LocalDate date, int monthCount)
			throws Exception {

		LocalDate beginDate = date.minusMonths(monthCount);
		StockHistory endH = stockHistoryService.findFirstHistoryBefore(allHistories, date, beginDate);
		StockHistory startH = stockHistoryService.findFirstHistoryBefore(allHistories, beginDate,
				beginDate.minusMonths(monthCount));

		if (endH == null || startH == null) {
			return -10000;
		}

		double diff = endH.close - startH.close;
		double perf = diff / startH.close;

		return perf * 100;
	}

}
