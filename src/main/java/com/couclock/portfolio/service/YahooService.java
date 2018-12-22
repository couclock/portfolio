package com.couclock.portfolio.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.couclock.portfolio.entity.FinStock;
import com.couclock.portfolio.entity.StockHistory;

import yahoofinance.histquotes.HistQuotesRequest;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes2.QueryInterval;
import yahoofinance.query2v8.HistQuotesQuery2V8Request;

@Service
public class YahooService {

	private static final Logger log = LoggerFactory.getLogger(YahooService.class);

	@Autowired
	private StockService stockService;
	@Autowired
	private StockHistoryService stockHistoryService;

	public void updateOneStockHistory(String stockCode, boolean considerLastHistory) throws IOException {

		FinStock stock = stockService.getByCode(stockCode);

		// Skip unknown stock
		if (stock == null) {
			return;
		}

		StockHistory lastStockHistory = stockHistoryService.getLatestHistory(stockCode);
		Map<LocalDate, StockHistory> date2History = stockHistoryService.getAllByStockCode_Map(stockCode);

		Calendar fromDate = Calendar.getInstance();

		if (lastStockHistory == null || !considerLastHistory) {
			fromDate.set(2000, 01, 01);
		} else {
			Date fDate = java.sql.Date.valueOf(lastStockHistory.date.plusDays(1));
			fromDate.setTime(fDate);
		}

		// En utilisant les API utilisés par les graphs
		HistQuotesQuery2V8Request historyData = new HistQuotesQuery2V8Request(stockCode + ".PA", fromDate,
				HistQuotesRequest.DEFAULT_TO, QueryInterval.DAILY);
		List<HistoricalQuote> histories = historyData.getResult();

		// En utilisant l'API des données historiques
//		Stock historyData = YahooFinance.get(stockCode + ".PA", fromDate, Interval.DAILY);
//		List<HistoricalQuote> histories = historyData.getHistory();

		List<StockHistory> toImport = new ArrayList<>();
		if (histories != null) {
			histories.forEach(oneDay -> {

				log.info("Handling : " + oneDay.getDate().getTime().toGMTString());

				// Skip incomplete data
				if (oneDay.getDate() == null || oneDay.getOpen() == null || oneDay.getClose() == null
						|| oneDay.getHigh() == null || oneDay.getLow() == null || oneDay.getVolume() == null) {
					log.info("Skipping (null) !");
					return;
				}
				// Skip invalid data
				if (oneDay.getOpen().doubleValue() == 0 && oneDay.getClose().doubleValue() == 0
						&& oneDay.getHigh().doubleValue() == 0 && oneDay.getLow().doubleValue() == 0
						&& oneDay.getVolume().longValue() == 0) {
					log.info("Skipping (0) !");
					return;
				}

				LocalDate lDate = new java.sql.Date(oneDay.getDate().getTime().getTime()).toLocalDate();

				// Skip known history
				if (date2History.containsKey(lDate)) {
					log.info("Skipping (known) !");

					return;
				}

				log.info(oneDay.getOpen() + " / " + oneDay.getClose() + " / " + oneDay.getVolume());

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
			updateOneStockHistory(oneStock.code, true);
		}

	}

}
