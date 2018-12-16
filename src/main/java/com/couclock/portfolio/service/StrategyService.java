package com.couclock.portfolio.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

			// Get Momentum on exUS stock and on sp500 stock
			double momentumExUS = getXMonthPerf(stock2H.get(exUsStockCode), currentDate.minusDays(1), 1)
					+ getXMonthPerf(stock2H.get(exUsStockCode), currentDate.minusDays(1), 3)
					+ getXMonthPerf(stock2H.get(exUsStockCode), currentDate.minusDays(1), 6);
			double momentumSP500 = getXMonthPerf(stock2H.get(usStockCode), currentDate.minusDays(1), 1)
					+ getXMonthPerf(stock2H.get(usStockCode), currentDate.minusDays(1), 3)
					+ getXMonthPerf(stock2H.get(usStockCode), currentDate.minusDays(1), 6);

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

			if (!pfStatus.containStock(targetStock)) {

				final LocalDate curDate = currentDate;
				List<MyStock> myStocks = new ArrayList<>(pfStatus.myStocks);
				myStocks.forEach(oneStock -> {
					StockHistory sh = findFirstHistoryAfter(stock2H.get(oneStock.stockCode), curDate,
							curDate.plusMonths(1));
					if (sh == null) {
						log.warn("Cannot sell : " + oneStock);
					} else {
						pfStatus.money += sh.open * oneStock.count;
						pfStatus.removeStock(oneStock.stockCode);
						pf.addSellEvent(curDate, oneStock.count, oneStock.stockCode);
					}
				});

				StockHistory sh = findFirstHistoryAfter(stock2H.get(targetStock), currentDate,
						currentDate.plusMonths(1));

				if (sh != null) {
					long count = Math.round(Math.floor(pfStatus.money / sh.open));
					if (count > 0) {
						pfStatus.addStock(count, targetStock);
						pfStatus.money -= count * sh.open;
						pf.addBuyEvent(curDate, count, targetStock);

					}
				}
			}

			List<PortfolioHistory> partialH = getPartialHistory(currentDate, currentDate.plusMonths(1), pfStatus,
					stock2H);
			pf.history.addAll(partialH);

			log.warn("Portfolio status : " + pfStatus);

			currentDate = currentDate.plusMonths(1);
		}

		pf.endDate = targetDate;
		pf.endStatus = pfStatus;
		log.warn("FINAL pf : " + pf);

		return pf;

	}

	/**
	 * Find history in submitted map equal or after to afterDate and before
	 * maxAfterDate<br/>
	 * afterDate < maxAfterDate
	 *
	 * @param allHistories
	 * @param beforeDate
	 * @param maxBeforeDate
	 * @return
	 */
	public StockHistory findFirstHistoryAfter(Map<LocalDate, StockHistory> allHistories, LocalDate afterDate,
			LocalDate maxAfterDate) {
		LocalDate current = LocalDate.from(afterDate);
		while (current.isBefore(maxAfterDate)) {
			if (allHistories.containsKey(current)) {
				return allHistories.get(current);
			}
			current = current.plusDays(1);
		}
		return null;
	}

	/**
	 * Find history in submitted map equal or before to beforeDate and after
	 * maxBeforeDate<br/>
	 * beforeDate > maxBeforeDate
	 *
	 * @param allHistories
	 * @param beforeDate
	 * @param maxBeforeDate
	 * @return
	 */
	public StockHistory findFirstHistoryBefore(Map<LocalDate, StockHistory> allHistories, LocalDate beforeDate,
			LocalDate maxBeforeDate) {
		LocalDate current = LocalDate.from(beforeDate);
		while (current.isAfter(maxBeforeDate)) {
			if (allHistories.containsKey(current)) {
				return allHistories.get(current);
			}
			current = current.minusDays(1);
		}
		return null;
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
	public double getXMonthPerf(Map<LocalDate, StockHistory> allHistories, LocalDate date, int monthCount)
			throws Exception {

		LocalDate beginDate = date.minusMonths(monthCount);
		StockHistory endH = findFirstHistoryBefore(allHistories, date, beginDate);
		StockHistory startH = findFirstHistoryBefore(allHistories, beginDate, beginDate.minusMonths(monthCount));

		if (endH == null || startH == null) {
			return -10000;
		}

		double diff = endH.close - startH.close;
		double perf = diff / startH.close;

		return perf * 100;
	}

	private List<PortfolioHistory> getPartialHistory(LocalDate startDate, LocalDate endDate, PortfolioStatus pfStatus,
			Map<String, Map<LocalDate, StockHistory>> stock2h) {

		List<PortfolioHistory> result = new ArrayList<>();
		LocalDate curDate = LocalDate.from(startDate);
		while (curDate.isBefore(endDate)) {

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
				result.add(new PortfolioHistory(curDate, dayValue));
			}

			curDate = curDate.plusDays(1);
		}

		return result;
	}

}
