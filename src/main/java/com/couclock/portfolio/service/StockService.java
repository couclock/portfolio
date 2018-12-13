package com.couclock.portfolio.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.couclock.portfolio.entity.FinStock;
import com.couclock.portfolio.repository.StockRepository;

@Service
public class StockService {

	private static final Logger log = LoggerFactory.getLogger(StockService.class);

	@Autowired
	private StockRepository stockRepository;

	public List<FinStock> findBySubstring(String substring) {
		return stockRepository
				.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(substring,
						substring, substring);

	}

	public List<FinStock> getAll() {
		return stockRepository.findAll();
	}

	public FinStock getByCode(String stockCode) {
		return stockRepository.findByCodeIgnoreCase(stockCode);
	}

	public void upsert(FinStock stock) {

		FinStock existing = stockRepository.findByCodeIgnoreCase(stock.code);
		if (existing == null) {
			stockRepository.save(stock);
		} else {
			existing.currency = stock.currency;
			existing.name = stock.name;
			existing.description = stock.description;
			existing.stockExchange = stock.stockExchange;
			stockRepository.save(existing);
		}

	}

}
