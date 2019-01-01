package com.couclock.portfolio.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.ROCIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import com.couclock.portfolio.entity.FinStock;
import com.couclock.portfolio.entity.StockHistory;
import com.couclock.portfolio.entity.StockIndicator;
import com.couclock.portfolio.repository.StockIndicatorRepository;

@Service
public class StockIndicatorService {

	private static final Logger log = LoggerFactory.getLogger(StockIndicatorService.class);
	private static final int BATCH_SIZE = 100;

	@Autowired
	private StockIndicatorRepository stockIndicatorRepository;

	@Autowired
	private StockHistoryService stockHistoryService;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private StockService stockService;

	@Transactional
	public void deleteByStock(String stockCode) {
		stockIndicatorRepository.deleteByStock_Code(stockCode);
	}

	/**
	 * Find first indicator in submitted map equal or before to beforeDate and after
	 * maxBeforeDate<br/>
	 * beforeDate > maxBeforeDate
	 *
	 * @param allIndicators
	 * @param beforeDate
	 * @param maxBeforeDate
	 * @return
	 */
	public StockIndicator findFirstIndicatorBefore(Map<LocalDate, StockIndicator> allIndicators, LocalDate beforeDate,
			LocalDate maxBeforeDate) {
		LocalDate current = LocalDate.from(beforeDate);
		while (current.isAfter(maxBeforeDate)) {
			if (allIndicators.containsKey(current)) {
				return allIndicators.get(current);
			}
			current = current.minusDays(1);
		}
		return null;
	}

	public Map<LocalDate, StockIndicator> getAllByStockCode_Map(String stockCode) {
		try {
			return stockIndicatorRepository.findByStock_CodeOrderByDateDesc(stockCode).stream() //
					.collect(Collectors.toMap(oneStockHistory -> oneStockHistory.date, Function.identity()));
		} catch (Exception e) {
			log.error("ERROR : ", e);
		}
		return null;
	}

	@Transactional
	public void updateIndicators(String stockCode, boolean resetExisting) {

		FinStock stock = stockService.getByCode(stockCode);

		// Skip unknown stock
		if (stock == null) {
			return;
		}

		if (resetExisting) {
			this.deleteByStock(stockCode);
		}

		List<StockHistory> histories = stockHistoryService.getAllByStockCode(stockCode);
		histories.sort(Collections.reverseOrder()); // To get older at the beginning
		if (histories.isEmpty()) {
			return;
		}
		StockHistory firstHistory = histories.get(0);
		StockIndicator lastIndicator = stockIndicatorRepository.findTop1ByStock_CodeOrderByDateDesc(stockCode);
		LocalDate currentDate = lastIndicator != null ? lastIndicator.date.plusDays(1) : firstHistory.date;
		LocalDate targetDate = LocalDate.now().plusDays(1);
		List<StockIndicator> toImport = new ArrayList<>();

		TimeSeries timeSeries = this.getTimeSeries(histories);
		ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
		ROCIndicator perf1M = new ROCIndicator(closePrice, 21); // 1M
		ROCIndicator perf3M = new ROCIndicator(closePrice, 63); // 3M
		ROCIndicator perf6M = new ROCIndicator(closePrice, 126); // 6M

		EMAIndicator ema6M = new EMAIndicator(closePrice, 126); // 6M

		Map<LocalDate, Integer> date2Index = new HashMap<>();
		for (int i = timeSeries.getBeginIndex(); i <= timeSeries.getEndIndex(); i++) {
			date2Index.put(timeSeries.getBar(i).getEndTime().toLocalDate(), i);
		}

		while (currentDate.isBefore(targetDate)) {

			// log.info("Handling " + currentDate + " indicators");
			if (date2Index.containsKey(currentDate)) {
				StockIndicator stockIndicator = new StockIndicator();
				stockIndicator.stock = stock;
				stockIndicator.date = currentDate;
				int idx = date2Index.get(currentDate);
				stockIndicator.perf1Month = perf1M.getValue(idx).doubleValue();
				stockIndicator.perf3Months = perf3M.getValue(idx).doubleValue();
				stockIndicator.perf6Months = perf6M.getValue(idx).doubleValue();
				stockIndicator.ema6Months = ema6M.getValue(idx).doubleValue();

				toImport.add(stockIndicator);
			}
			currentDate = currentDate.plusDays(1);
		}

		log.warn("Stock Indicator toImport count : " + toImport.size());

		createBatch(toImport);
		log.warn("Done !");

	}

	private void createBatch(List<StockIndicator> entities) {
		int i = 0;
		for (StockIndicator t : entities) {
			entityManager.persist(t);
			i++;
			if (i % BATCH_SIZE == 0) {
				// Flush a batch of inserts and release memory.
				entityManager.flush();
				entityManager.clear();
			}
		}
	}

	private TimeSeries getTimeSeries(List<StockHistory> histories) {

		TimeSeries series = new BaseTimeSeries.SeriesBuilder().build();

		for (StockHistory oneHistory : histories) {
			series.addBar(oneHistory.date.atTime(18, 0).atZone(ZoneId.of("Europe/Paris")), oneHistory.open,
					oneHistory.high, oneHistory.low, oneHistory.close, oneHistory.volume);
		}

		return series;

	}

}
