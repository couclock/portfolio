package com.couclock.portfolio.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.couclock.portfolio.entity.FinStock;
import com.couclock.portfolio.entity.StockHistory;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.Interval;

@Service
public class YahooService {

	private static final Logger log = LoggerFactory.getLogger(YahooService.class);

	@Autowired
	private StockService stockService;
	@Autowired
	private StockHistoryService stockHistoryService;

	public void updateOneStockHistory(String stockCode) throws IOException {

		FinStock stock = stockService.getByCode(stockCode);

		// Skip unknown stock
		if (stock == null) {
			return;
		}

		StockHistory lastStockHistory = stockHistoryService.getLatestHistory(stockCode);

		Calendar fromDate = Calendar.getInstance();

		if (lastStockHistory == null) {
			fromDate.set(2000, 01, 01);
		} else {
			Date fDate = java.sql.Date.valueOf(lastStockHistory.date.plusDays(1));
			fromDate.setTime(fDate);
		}

		Stock stockData = YahooFinance.get(stockCode + ".PA", fromDate, Interval.DAILY);

		List<StockHistory> toImport = new ArrayList<>();
		if (stockData.getHistory() != null) {
			stockData.getHistory().forEach(oneDay -> {

				// Skip incomplete data
				if (oneDay.getDate() == null || oneDay.getOpen() == null || oneDay.getClose() == null
						|| oneDay.getHigh() == null || oneDay.getLow() == null || oneDay.getVolume() == null) {
					return;
				}

				LocalDate lDate = new java.sql.Date(oneDay.getDate().getTime().getTime()).toLocalDate();

				// Skip last known history
				if (lastStockHistory != null
						&& (lDate.isEqual(lastStockHistory.date) || lDate.isBefore(lastStockHistory.date))) {
					return;
				}

				StockHistory newStockHistory = new StockHistory();
				newStockHistory.stock = stock;
				newStockHistory.date = lDate;
				newStockHistory.open = oneDay.getOpen().doubleValue();
				newStockHistory.high = oneDay.getHigh().doubleValue();
				newStockHistory.low = oneDay.getLow().doubleValue();
				newStockHistory.close = oneDay.getClose().doubleValue();
				newStockHistory.volume = oneDay.getVolume().longValue();

				toImport.add(newStockHistory);

			});
		}

		log.warn("toImport count : " + toImport.size());

		stockHistoryService.createBatch(toImport);

	}

	public void updateStocksHistory() throws IOException {

		List<FinStock> stocks = stockService.getAll();
		int count = stocks.size();
		int current = 1;

		for (FinStock oneStock : stocks) {
			log.warn("[" + current++ + "/" + count + "] Updating " + oneStock.code);
			updateOneStockHistory(oneStock.code);
		}

	}

}
