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
import com.couclock.portfolio.entity.StockIndicator;
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

	@Autowired
	private StockIndicatorService stockIndicatorService;

	public Portfolio acceleratedDualMomentum(Portfolio pf) throws Exception {

		LocalDate currentDate = LocalDate.from(pf.endDate);
		LocalDate targetDate = LocalDate.now().plusDays(1); // To stop loop after considering today
		PortfolioStatus pfStatus = pf.currentStatus;

		AcceleratedMomentumStrategy strategyParameters = (AcceleratedMomentumStrategy) pf.strategyParameters;

		List<StockDistribution> usStocks = strategyParameters.usStocks;
		List<StockDistribution> exUsStocks = strategyParameters.exUsStocks;
		List<StockDistribution> bondStocks = strategyParameters.bondStocks;

		Map<String, Map<LocalDate, StockHistory>> stock2H = new HashMap<>();
		Map<String, Map<LocalDate, StockIndicator>> stock2I = new HashMap<>();
		pf.strategyParameters.getStockList().forEach(oneStock -> {
			stock2H.put(oneStock, stockHistoryService.getAllByStockCode_Map(oneStock));
			stock2I.put(oneStock, stockIndicatorService.getAllByStockCode_Map(oneStock));
		});

		while (currentDate.isBefore(targetDate)) {

			boolean monthLastDay = currentDate.plusDays(1).getDayOfMonth() == 1;

			// On month last day, calculate momentums
			if (monthLastDay) {

				List<StockDistribution> targetStocks = getToBuyStockOnNextMonthStart(currentDate, stock2H, stock2I,
						usStocks, exUsStocks, bondStocks);
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

			// Check protection rules
			checkProtectionRules(currentDate, pfStatus, stock2H, stock2I);

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

				if (stockToBuyToday.size() != pfStatus.toBuy.size()) {
					log.error("Stock distribution will be invalid ...");
				}

				final double moneyToBuy = pfStatus.money;

				stockToBuyToday.forEach(oneStockToBuy -> {
					StockHistory oneHistory = stock2H.get(oneStockToBuy.stockCode).get(curDate);
					log.info("toBuy stock history : " + oneHistory);

					long count = Math.round(Math.floor((moneyToBuy * oneStockToBuy.percent) / oneHistory.open));
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
	 * Check if current portfolio contains only stocks validating protection rules
	 *
	 * @param curDate
	 * @param pfStatus
	 * @param stock2h
	 * @param stock2i
	 */
	private void checkProtectionRules(LocalDate curDate, PortfolioStatus pfStatus,
			Map<String, Map<LocalDate, StockHistory>> stock2h, Map<String, Map<LocalDate, StockIndicator>> stock2i) {

		pfStatus.currentStocks.forEach(oneStock -> {
			if (stock2h.containsKey(oneStock.stockCode) && stock2h.get(oneStock.stockCode).containsKey(curDate)
					&& stock2i.containsKey(oneStock.stockCode)
					&& stock2i.get(oneStock.stockCode).containsKey(curDate)) {
				StockHistory stockHistory = stock2h.get(oneStock.stockCode).get(curDate);
				StockIndicator stockIndicator = stock2i.get(oneStock.stockCode).get(curDate);

				// Rule 1 : Do not keep stock closing under its ema - months
				// 0,96 pour 500_ESM
				// 0,97 pour RS2K_SMC
				if (stockHistory.close < stockIndicator.ema6Months * 0.96) {

					log.info("checkProtectionRules : PROTECTION : " + oneStock.stockCode);

					// Add to toSell list
					if (!pfStatus.toSell.contains(oneStock.stockCode)) {
						pfStatus.toSell.add(oneStock.stockCode);
					}
					// Remove from toBuy List
					pfStatus.toBuy = pfStatus.toBuy.stream() //
							.filter(oneStockToBuy -> {
								return oneStockToBuy.stockCode == oneStock.stockCode;
							}) //
							.collect(Collectors.toList());
				}
			}
		});
	}

	/**
	 * Get stocks to buy considering accelerated momentum strategy
	 *
	 * @param currentDate Last day of current month
	 * @param stock2H
	 * @param stock2i
	 * @param usStocks
	 * @param exUsStocks
	 * @param bondStocks
	 * @return
	 * @throws Exception
	 */
	private List<StockDistribution> getToBuyStockOnNextMonthStart(LocalDate currentDate,
			Map<String, Map<LocalDate, StockHistory>> stock2H, Map<String, Map<LocalDate, StockIndicator>> stock2i,
			List<StockDistribution> usStocks, List<StockDistribution> exUsStocks, List<StockDistribution> bondStocks)
			throws Exception {

		// Get Momentum on exUS stock and on sp500 stock
		double momentumExUS = 0;
		for (StockDistribution oneStock : exUsStocks) {
			StockIndicator stockIndicator = stockIndicatorService
					.findFirstIndicatorBefore(stock2i.get(oneStock.stockCode), currentDate, currentDate.minusMonths(1));

			if (stockIndicator != null) {
				momentumExUS += oneStock.percent
						* (stockIndicator.perf1Month + stockIndicator.perf3Months + stockIndicator.perf6Months);
			}

		}
		double momentumUS = 0;
		for (StockDistribution oneStock : usStocks) {
			StockIndicator stockIndicator = stockIndicatorService
					.findFirstIndicatorBefore(stock2i.get(oneStock.stockCode), currentDate, currentDate.minusMonths(1));
			if (stockIndicator != null) {
				momentumUS += oneStock.percent
						* (stockIndicator.perf1Month + stockIndicator.perf3Months + stockIndicator.perf6Months);
			}
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

		// log.info("getTodayHistory(" + curDate + ") : " + dayValue + "#" + allFound);

		if (allFound) {
			return new PortfolioHistory(curDate, dayValue);
		} else {
			return null;
		}

	}

}
