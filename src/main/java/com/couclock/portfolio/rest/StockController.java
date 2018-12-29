package com.couclock.portfolio.rest;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.couclock.portfolio.entity.FinStock;
import com.couclock.portfolio.entity.StockHistory;
import com.couclock.portfolio.service.BoursoService;
import com.couclock.portfolio.service.QuandlService;
import com.couclock.portfolio.service.StockHistoryService;
import com.couclock.portfolio.service.StockService;
import com.couclock.portfolio.service.YahooService;

@RestController
@RequestMapping("/stocks")
@CrossOrigin(origins = "*")
public class StockController {

	@Autowired
	private YahooService yahooService;

	@Autowired
	private QuandlService quandlService;

	@Autowired
	private BoursoService boursoService;

	@Autowired
	private StockService stockService;

	@Autowired
	private StockHistoryService stockHistoryService;

	@RequestMapping(method = RequestMethod.POST)
	public String addOne(@RequestBody FinStock newStock) throws Exception {

		stockService.addOne(newStock);

		return "ok";

	}

	@RequestMapping(method = RequestMethod.POST, value = "/{stockCode}")
	public String addOne(@PathVariable(value = "stockCode") String stockCode) throws Exception {

		stockService.addStock(stockCode);

		return "ok";

	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/{stockCode}")
	public String deleteOne(@PathVariable(value = "stockCode") String stockCode) {

		this.reset(stockCode);
		stockService.deleteByCode(stockCode);

		return "ok";

	}

	@RequestMapping("/")
	public List<FinStock> getAll() {

		return stockService.getAll();

	}

	@RequestMapping("/{stockCode}")
	public FinStock getOne(@PathVariable(value = "stockCode") String stockCode) {

		return stockService.getByCode(stockCode);

	}

	@RequestMapping("/{stockCode}/history")
	public List<StockHistory> getOneHistory(@PathVariable(value = "stockCode") String stockCode) {

		return stockHistoryService.getAllByStockCode(stockCode);

	}

	@RequestMapping("/{stockCode}/history/last")
	public StockHistory getOneLastHistory(@PathVariable(value = "stockCode") String stockCode) {

		return stockHistoryService.getLatestHistory(stockCode);

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

	@RequestMapping(method = RequestMethod.POST, value = "/{stockCode}/reset")
	public String reset(@PathVariable(value = "stockCode") String stockCode) {

		stockHistoryService.deleteByStock(stockCode);

		return "ok";

	}

	@RequestMapping(method = RequestMethod.POST, value = "/update2")
	public String updateAll_quandl() throws IOException {

		quandlService.updateStocksHistory();

		return "ok";

	}

	@RequestMapping(method = RequestMethod.POST, value = "/update")
	public String updateAll_yahoo() throws IOException {

		yahooService.updateStocksHistory();

		return "ok";

	}

	@RequestMapping(method = RequestMethod.POST, value = "/{stockCode}/update_bourso")
	public void updateOne_bourso(@PathVariable(value = "stockCode") String stockCode) throws IOException {

		boursoService.getStockHistory(stockCode);

	}

	@RequestMapping(method = RequestMethod.POST, value = "/{stockCode}/update2")
	public List<StockHistory> updateOne_quandl(@PathVariable(value = "stockCode") String stockCode) throws IOException {

		quandlService.updateOneStockHistory(stockCode);

		return stockHistoryService.getAllByStockCode(stockCode);

	}

	@RequestMapping(method = RequestMethod.POST, value = "/{stockCode}/update")
	public List<StockHistory> updateOne_yahoo(@PathVariable(value = "stockCode") String stockCode) throws IOException {

		yahooService.updateOneStockHistory(stockCode, false);

		return stockHistoryService.getAllByStockCode(stockCode);

	}
}
