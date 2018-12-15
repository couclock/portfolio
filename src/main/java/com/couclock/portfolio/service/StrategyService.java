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

import com.couclock.portfolio.dto.PortfolioStatusDTO;
import com.couclock.portfolio.dto.PortfolioStatusDTO.MyStock;
import com.couclock.portfolio.entity.Portfolio;
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

	public Portfolio acceleratedDualMomentum(String sp500StockCode, String exUSStockCode, String bondStockCode)
			throws Exception {

		LocalDate currentDate = LocalDate.parse("2005-01-01");
		PortfolioStatusDTO pfStatus = new PortfolioStatusDTO();
		pfStatus.money = 10000;

		Portfolio portfolio = new Portfolio();
		portfolio.startDate = currentDate;
		portfolio.startMoney = pfStatus.money;
		portfolio.addAddMoneyEvent(currentDate, pfStatus.money);

		Map<LocalDate, StockHistory> exUSH = stockHistoryService.getAllByStockCode_Map(exUSStockCode);
		Map<LocalDate, StockHistory> sp500H = stockHistoryService.getAllByStockCode_Map(sp500StockCode);
		Map<LocalDate, StockHistory> bondH = stockHistoryService.getAllByStockCode_Map(bondStockCode);
		Map<String, Map<LocalDate, StockHistory>> stock2H = new HashMap<>();
		stock2H.put(exUSStockCode, exUSH);
		stock2H.put(sp500StockCode, sp500H);
		stock2H.put(bondStockCode, bondH);

		while (currentDate.isBefore(LocalDate.now())) {
			double momentumExUS = getXMonthPerf(exUSH, currentDate.minusDays(1), 1)
					+ getXMonthPerf(exUSH, currentDate.minusDays(1), 3)
					+ getXMonthPerf(exUSH, currentDate.minusDays(1), 6);
			double momentumSP500 = getXMonthPerf(sp500H, currentDate.minusDays(1), 1)
					+ getXMonthPerf(sp500H, currentDate.minusDays(1), 3)
					+ getXMonthPerf(sp500H, currentDate.minusDays(1), 6);

			String targetStock = null;

			if (momentumSP500 > momentumExUS) {
				if (momentumSP500 > 0) {
					targetStock = sp500StockCode;
				} else {
					targetStock = bondStockCode;
				}
			} else {
				if (momentumExUS > 0) {
					targetStock = exUSStockCode;
				} else {
					targetStock = bondStockCode;
				}
			}

			log.warn("ToBuy/hold on " + currentDate + " : " + targetStock);

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
						portfolio.addSellEvent(curDate, oneStock.count, oneStock.stockCode);
					}
				});

				StockHistory sh = findFirstHistoryAfter(stock2H.get(targetStock), currentDate,
						currentDate.plusMonths(1));

				if (sh != null) {
					long count = Math.round(Math.floor(pfStatus.money / sh.open));
					if (count > 0) {
						pfStatus.addStock(count, targetStock);
						pfStatus.money -= count * sh.open;
						portfolio.addBuyEvent(curDate, count, targetStock);

					}
				}
			}

			log.warn("Portfolio status : " + pfStatus);

			currentDate = currentDate.plusMonths(1);
		}

		// get portfolio value with a sell all
//		final LocalDate curDate = currentDate;
//
//		List<MyStock> myStocks = new ArrayList<>(currentPF.myStocks);
//		myStocks.forEach(oneStock -> {
//			StockHistory sh = findFirstHistoryBefore(stock2H.get(oneStock.stockCode), curDate, curDate.minusMonths(1));
//			if (sh == null) {
//				log.warn("Cannot sell : " + oneStock);
//			} else {
//				currentPF.money += sh.open * oneStock.count;
//				currentPF.removeStock(oneStock.stockCode);
//				pfHistory.addSellEvent(curDate, oneStock.count, oneStock.stockCode, currentPF);
//
//			}
//		});

		log.warn("FINAL pfHistory : " + portfolio);

		return portfolio;

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

}
