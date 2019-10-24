package com.couclock.portfolio.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.couclock.portfolio.entity.StockHistory;
import com.couclock.portfolio.repository.StockHistoryRepository;

@Service
public class StockHistoryService {

	private static final Logger log = LoggerFactory.getLogger(StockHistoryService.class);

	private final static int BATCH_SIZE = 100;

	private Map<String, List<StockHistory>> historyCache = new HashMap<>();

	@Autowired
	private StockHistoryRepository stockHistoryRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public void createBatch(List<StockHistory> entities) {
		int i = 0;
		for (StockHistory t : entities) {
			entityManager.persist(t);
			i++;
			if (i % BATCH_SIZE == 0) {
				// Flush a batch of inserts and release memory.
				entityManager.flush();
				entityManager.clear();
			}
		}
		historyCache.clear();
	}

	@Transactional
	public void deleteByStock(String stockCode) {
		stockHistoryRepository.deleteByStock_Code(stockCode);
		historyCache.remove(stockCode);

	}

	@Transactional
	public void deleteByStockId(long stockId) {
		stockHistoryRepository.deleteByStock_Id(stockId);
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

	public StockHistory findFirstHistoryAfter(String stockCode, LocalDate afterDate, LocalDate maxAfterDate) {

		if (!historyCache.containsKey(stockCode)) {
			historyCache.put(stockCode, stockHistoryRepository.findByStock_CodeOrderByDateAsc(stockCode));
		}

		Optional<StockHistory> historyFound = historyCache.get(stockCode).stream() //
				.sorted((h1, h2) -> {
					return h1.date.compareTo(h2.date);
				}) //
				.filter(oneHistory -> {
					return oneHistory.date.isEqual(afterDate) //
							|| (oneHistory.date.isAfter(afterDate) //
									&& (maxAfterDate == null || oneHistory.date.isBefore(maxAfterDate)));
				}).findFirst();

		return historyFound.isPresent() ? historyFound.get() : null;
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

	public StockHistory findFirstHistoryBefore(String stockCode, LocalDate beforeDate, LocalDate maxBeforeDate) {

		if (!historyCache.containsKey(stockCode)) {
			historyCache.put(stockCode, stockHistoryRepository.findByStock_CodeOrderByDateAsc(stockCode));
		}

		Optional<StockHistory> historyFound = historyCache.get(stockCode).stream() //
				.sorted((h1, h2) -> {
					return h1.date.compareTo(h2.date) * -1;
				}) //
				.filter(oneHistory -> {
					return oneHistory.date.isEqual(beforeDate) //
							|| (oneHistory.date.isBefore(beforeDate) //
									&& (maxBeforeDate == null || oneHistory.date.isAfter(maxBeforeDate)));
				}).findFirst();

		return historyFound.isPresent() ? historyFound.get() : null;
	}

	public List<StockHistory> getAllByStockCode(String stockCode) {
		return stockHistoryRepository.findByStock_CodeOrderByDateDesc(stockCode);
	}

	/**
	 * Retourne l'historique d'un ETF sous la forme attendue par la lib de graph
	 * côté front (highcharts) Format attendu : [[timestamp1, val1], [timestamp2,
	 * val2] ...]
	 * 
	 * @param stockCode
	 * @param startMoney
	 * @param startDate
	 * @return
	 */
	public List<List<Number>> getHistoryForGraph(String stockCode, double startMoney, LocalDate startDate) {
		List<StockHistory> history = stockHistoryRepository.findByStock_CodeAndDateAfterOrderByDateAsc(stockCode,
				startDate);

		List<List<Number>> result = new ArrayList<>();
		double previousCloseStockValue = 0;
		double currentValue = startMoney;

		for (StockHistory stockHistory : history) {
			if (previousCloseStockValue != 0) {
				currentValue = currentValue
						* (1 + (stockHistory.close - previousCloseStockValue) / previousCloseStockValue);
			}
			previousCloseStockValue = stockHistory.close;
			result.add(Arrays.asList(stockHistory.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
					currentValue));

		}

		return result;
	}

	public Map<LocalDate, StockHistory> getAllByStockCode_Map(String stockCode) {
		try {
			return stockHistoryRepository.findByStock_CodeOrderByDateDesc(stockCode).stream() //
					.collect(Collectors.toMap(oneStockHistory -> oneStockHistory.date, Function.identity()));
		} catch (Exception e) {
			log.error("ERROR : ", e);
		}
		return null;
	}

	public StockHistory getLatestHistory(String stockCode) {
		return stockHistoryRepository.findTop1ByStock_CodeOrderByDateDesc(stockCode);
	}

	public StockHistory getLatestHistoryById(long stockId) {
		return stockHistoryRepository.findTop1ByStock_IdOrderByDateDesc(stockId);
	}

}
