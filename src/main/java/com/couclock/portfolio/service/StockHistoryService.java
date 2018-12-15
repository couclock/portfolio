package com.couclock.portfolio.service;

import java.time.LocalDate;
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

import com.couclock.portfolio.entity.StockHistory;
import com.couclock.portfolio.repository.StockHistoryRepository;

@Service
public class StockHistoryService {

	private static final Logger log = LoggerFactory.getLogger(StockHistoryService.class);

	@Autowired
	private StockHistoryRepository stockHistoryRepository;

	@PersistenceContext
	private EntityManager entityManager;

	private int batchSize = 100;

	public void create(StockHistory stockHistory) {

		stockHistoryRepository.save(stockHistory);

	}

	@Transactional
	public void createBatch(List<StockHistory> entities) {
		int i = 0;
		for (StockHistory t : entities) {
			entityManager.persist(t);
			i++;
			if (i % batchSize == 0) {
				// Flush a batch of inserts and release memory.
				entityManager.flush();
				entityManager.clear();
			}
		}
	}

	public List<StockHistory> getAllByStockCode(String stockCode) {
		return stockHistoryRepository.findByStock_CodeOrderByDateDesc(stockCode);
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

	public void upsert(StockHistory stockHistory) {

		StockHistory existing = stockHistoryRepository.findByStock_CodeAndDate(stockHistory.stock.code,
				stockHistory.date);
		if (existing == null) {
			create(stockHistory);
		} else {
			existing.open = stockHistory.open;
			existing.high = stockHistory.high;
			existing.low = stockHistory.low;
			existing.close = stockHistory.close;
			existing.volume = stockHistory.volume;

			stockHistoryRepository.save(existing);
		}

	}

}
