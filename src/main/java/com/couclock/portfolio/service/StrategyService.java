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
import com.couclock.portfolio.entity.PortfolioStatus.MyStock;
import com.couclock.portfolio.entity.StockHistory;

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
		PortfolioStatus pfStatus = pf.endStatus;

		String usStockCode = pf.usStockCode;
		String exUsStockCode = pf.exUsStockCode;
		String bondStockCode = pf.bondStockCode;

		Map<String, Map<LocalDate, StockHistory>> stock2H = new HashMap<>();
		stock2H.put(exUsStockCode, stockHistoryService.getAllByStockCode_Map(exUsStockCode));
		stock2H.put(usStockCode, stockHistoryService.getAllByStockCode_Map(usStockCode));
		stock2H.put(bondStockCode, stockHistoryService.getAllByStockCode_Map(bondStockCode));

		while (currentDate.isBefore(targetDate)) {

			boolean monthLastDay = currentDate.plusDays(1).getDayOfMonth() == 1;

			// On month last day, calculate momentums
			if (monthLastDay) {

				String targetStock = getToBuyStockOnNextMonthStart(currentDate, stock2H, usStockCode, exUsStockCode,
						bondStockCode);
				pfStatus.toSell.remove(targetStock);
				if (pfStatus.containStock(targetStock)) {
					;
				} else {
					pfStatus.toBuy.clear();
					pfStatus.toBuy.add(targetStock);
					pfStatus.myStocks.forEach(oneStock -> {
						if (!pfStatus.toSell.contains(oneStock.stockCode)) {
							pfStatus.toSell.add(oneStock.stockCode);
						}
					});
				}
				log.info("Selected targetStock for next month [" + currentDate + "] : " + targetStock);

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
				List<String> stockToBuyToday = pfStatus.toBuy.stream() //
						.filter(oneStockCode -> stock2H.get(oneStockCode).containsKey(curDate)) //
						.collect(Collectors.toList());

				stockToBuyToday.forEach(oneStockToBuy -> {
					StockHistory oneHistory = stock2H.get(oneStockToBuy).get(curDate);
					log.info("toBuy stock history : " + oneHistory);

					long count = Math.round(Math.floor(pfStatus.money / oneHistory.open));
					if (count > 0) {
						pfStatus.addStock(count, oneStockToBuy);
						pfStatus.money -= count * oneHistory.open;
						pf.addBuyEvent(curDate, count, oneStockToBuy);

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
		pf.endStatus = pfStatus;
		log.warn("FINAL pf : " + pf);

		return pf;

	}

	/**
	 * Get stocks to buy considering accelereted momentum strategy
	 *
	 * @param currentDate   Last day of current month
	 * @param stock2H
	 * @param usStockCode
	 * @param exUsStockCode
	 * @param bondStockCode
	 * @return
	 * @throws Exception
	 */
	private String getToBuyStockOnNextMonthStart(LocalDate currentDate,
			Map<String, Map<LocalDate, StockHistory>> stock2H, String usStockCode, String exUsStockCode,
			String bondStockCode) throws Exception {

		// Get Momentum on exUS stock and on sp500 stock
		double momentumExUS = getXMonthPerf(stock2H.get(exUsStockCode), currentDate, 1)
				+ getXMonthPerf(stock2H.get(exUsStockCode), currentDate, 3)
				+ getXMonthPerf(stock2H.get(exUsStockCode), currentDate, 6);
		double momentumSP500 = getXMonthPerf(stock2H.get(usStockCode), currentDate, 1)
				+ getXMonthPerf(stock2H.get(usStockCode), currentDate, 3)
				+ getXMonthPerf(stock2H.get(usStockCode), currentDate, 6);

		// Set targetStock
		String targetStock = null;
		if (momentumSP500 > momentumExUS) {
			if (momentumSP500 > 0) {
				targetStock = usStockCode;
			} else {
				targetStock = bondStockCode;
			}
		} else {
			if (momentumExUS > 0) {
				targetStock = exUsStockCode;
			} else {
				targetStock = bondStockCode;
			}
		}

		return targetStock;
	}

	private PortfolioHistory getTodayHistory(LocalDate curDate, PortfolioStatus pfStatus,
			Map<String, Map<LocalDate, StockHistory>> stock2h) {

		double dayValue = pfStatus.money;

		boolean allFound = true;

		for (MyStock oneStock : pfStatus.myStocks) {
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
