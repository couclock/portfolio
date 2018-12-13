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

import com.couclock.portfolio.dto.PortfolioDTO;
import com.couclock.portfolio.dto.PortfolioDTO.MyStock;
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

	public void acceleratedDualMomentum() throws Exception {

		LocalDate currentDate = LocalDate.parse("2010-01-01");
		PortfolioDTO portfolio = new PortfolioDTO();
		portfolio.money = 10000;

		Map<LocalDate, StockHistory> mmsH = stockHistoryService.getAllByStockCode_Map("MMS");
		Map<LocalDate, StockHistory> c500H = stockHistoryService.getAllByStockCode_Map("500");
		Map<LocalDate, StockHistory> ustyH = stockHistoryService.getAllByStockCode_Map("USTY");
		Map<String, Map<LocalDate, StockHistory>> stock2H = new HashMap<>();
		stock2H.put("MMS", mmsH);
		stock2H.put("500", c500H);
		stock2H.put("USTY", ustyH);

		while (currentDate.isBefore(LocalDate.now())) {
			double momentumMMS = getXMonthPerf(mmsH, currentDate.minusDays(1), 1)
					+ getXMonthPerf(mmsH, currentDate.minusDays(1), 3)
					+ getXMonthPerf(mmsH, currentDate.minusDays(1), 6);
			double momentum500 = getXMonthPerf(c500H, currentDate.minusDays(1), 1)
					+ getXMonthPerf(c500H, currentDate.minusDays(1), 3)
					+ getXMonthPerf(c500H, currentDate.minusDays(1), 6);

			String targetStock = null;

			if (momentum500 > momentumMMS) {
				if (momentum500 > 0) {
					targetStock = "500";
				} else {
					targetStock = "USTY";
				}
			} else {
				if (momentumMMS > 0) {
					targetStock = "MMS";
				} else {
					targetStock = "USTY";
				}
			}

			log.warn("ToBuy/hold on " + currentDate + " : " + targetStock);

			if (!portfolio.containStock(targetStock)) {

				boolean buyToDo = true;

				final LocalDate curDate = currentDate;
				List<MyStock> myStocks = new ArrayList<>(portfolio.myStocks);
				myStocks.forEach(oneStock -> {
					StockHistory sh = findFirstHistoryAfter(stock2H.get(oneStock.stockCode), curDate,
							curDate.plusMonths(1));
					if (sh == null) {
						log.warn("Cannot sell : " + oneStock);
					} else {
						portfolio.money += sh.open * oneStock.count;
						portfolio.removeStock(oneStock.stockCode);
					}
				});

				StockHistory sh = findFirstHistoryAfter(stock2H.get(targetStock), currentDate,
						currentDate.plusMonths(1));

				if (sh != null) {
					long count = Math.round(Math.floor(portfolio.money / sh.open));
					if (count > 0) {
						portfolio.addStock(count, targetStock);
						portfolio.money -= count * sh.open;
					}
				}
			}

			log.warn("Portfolio status : " + portfolio);

			currentDate = currentDate.plusMonths(1);
		}

		// get portfolio value with a sell all
		final LocalDate curDate = currentDate;

		List<MyStock> myStocks = new ArrayList<>(portfolio.myStocks);
		myStocks.forEach(oneStock -> {
			StockHistory sh = findFirstHistoryBefore(stock2H.get(oneStock.stockCode), curDate, curDate.minusMonths(1));
			if (sh == null) {
				log.warn("Cannot sell : " + oneStock);
			} else {
				portfolio.money += sh.open * oneStock.count;
				portfolio.removeStock(oneStock.stockCode);
			}
		});

		log.warn("FINAL Portfolio status : " + portfolio);

	}

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
