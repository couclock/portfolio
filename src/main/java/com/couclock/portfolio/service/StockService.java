package com.couclock.portfolio.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.couclock.portfolio.entity.FinStock;
import com.couclock.portfolio.repository.StockRepository;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

@Service
public class StockService {

	private static final Logger log = LoggerFactory.getLogger(StockService.class);

	@Autowired
	private StockRepository stockRepository;

	public void addOne(FinStock newStock) throws Exception {

		Stock stock = null;
		try {
			stock = YahooFinance.get(newStock.code.toUpperCase() + ".PA");
		} catch (Exception e) {
			throw new Exception("Invalid stock code");
		} finally {
			if (stock != null && !stock.isValid()) {
				throw new Exception("Invalid stock code");
			}
		}

		newStock.code = newStock.code.toUpperCase();
		newStock.name = stock.getName();
		newStock.currency = stock.getCurrency();
		newStock.stockExchange = stock.getStockExchange();

		this.upsert(newStock);
	}

	public void addStock(String stockCode) throws Exception {

		Stock stock = null;
		try {
			stock = YahooFinance.get(stockCode.toUpperCase() + ".PA");
		} catch (Exception e) {
			throw new Exception("Invalid stock code");
		} finally {
			if (!stock.isValid()) {
				throw new Exception("Invalid stock code");
			}
		}

		FinStock finStock = new FinStock();
		finStock.code = stockCode.toUpperCase();
		finStock.name = stock.getName();
		finStock.currency = stock.getCurrency();
		finStock.stockExchange = stock.getStockExchange();

		this.upsert(finStock);
	}

	public void deleteByCode(String stockCode) {
		FinStock stock = stockRepository.findByCodeIgnoreCase(stockCode);
		if (stock != null) {
			stockRepository.delete(stock);
		}
	}

	public void deleteById(long stockId) {
		stockRepository.deleteById(stockId);
	}

	public List<FinStock> findBySubstring(String substring) {
		return stockRepository
				.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(substring,
						substring, substring);

	}

	public List<FinStock> findByTag(String tag) {
		return stockRepository.findByTagsIgnoreCaseOrderByCode(tag);

	}

	public List<FinStock> getAll() {
		return stockRepository.findAll().stream() //
				.sorted((stock1, stock2) -> stock1.code.compareTo(stock2.code)) //
				.collect(Collectors.toList());
	}

	public FinStock getByCode(String stockCode) {
		return stockRepository.findByCodeIgnoreCase(stockCode);
	}

	public FinStock getByStockId(long stockId) {
		Optional<FinStock> stockToReturn = stockRepository.findById(stockId);
		return stockToReturn.isPresent() ? stockToReturn.get() : null;
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
			existing.tags = stock.tags;
			stockRepository.save(existing);
		}

	}

}
