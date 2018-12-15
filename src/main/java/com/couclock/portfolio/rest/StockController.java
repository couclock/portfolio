package com.couclock.portfolio.rest;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.couclock.portfolio.entity.FinStock;
import com.couclock.portfolio.entity.StockHistory;
import com.couclock.portfolio.service.StockHistoryService;
import com.couclock.portfolio.service.StockService;
import com.couclock.portfolio.service.YahooService;

@RestController
@RequestMapping("/stocks")
public class StockController {

	@Autowired
	private YahooService yahooService;

	@Autowired
	private StockService stockService;

	@Autowired
	private StockHistoryService stockHistoryService;

	@RequestMapping("/{stockCode}")
	public FinStock getOne(@PathVariable(value = "stockCode") String stockCode) {

		return stockService.getByCode(stockCode);

	}

	@RequestMapping("/{stockCode}/history")
	public List<StockHistory> getOneHistory(@PathVariable(value = "stockCode") String stockCode) {

		return stockHistoryService.getAllByStockCode(stockCode);

	}

	@RequestMapping("/query")
	public List<FinStock> query(@RequestParam(value = "q") String q) throws IOException {

		return stockService.findBySubstring(q);

//		Stock stock = YahooFinance.get(q);
//
//		FinStock finStock = new FinStock();
//		finStock.name = stock.getName();
//		finStock.code = stock.getSymbol();
//		finStock.currency = stock.getCurrency();
//		finStock.stockExchange = stock.getStockExchange();
//
//		return finStock;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{stockCode}/update")
	public List<StockHistory> update(@PathVariable(value = "stockCode") String stockCode) throws IOException {

		yahooService.updateOneStockHistory(stockCode);

		return stockHistoryService.getAllByStockCode(stockCode);

	}

	@RequestMapping(method = RequestMethod.POST, value = "/update")
	public String updateAll() throws IOException {

		yahooService.updateStocksHistory();

		return "Ok, done !";

	}
}
