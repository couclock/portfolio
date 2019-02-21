package com.couclock.portfolio.rest;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.couclock.portfolio.dto.LightStockDTO;
import com.couclock.portfolio.entity.FinStock;
import com.couclock.portfolio.entity.StockHistory;
import com.couclock.portfolio.service.BoursoService;
import com.couclock.portfolio.service.SchedulingService;
import com.couclock.portfolio.service.StockHistoryService;
import com.couclock.portfolio.service.StockIndicatorService;
import com.couclock.portfolio.service.StockService;
import com.couclock.portfolio.service.YahooService;

@RestController
@RequestMapping("/stocks")
@CrossOrigin(origins = "*")
public class StockController {

	@Autowired
	private YahooService yahooService;

	@Autowired
	private BoursoService boursoService;

	@Autowired
	private StockService stockService;
	@Autowired
	private SchedulingService schedulingService;

	@Autowired
	private StockIndicatorService stockIndicatorService;

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

	@RequestMapping(method = RequestMethod.DELETE)
	public List<Long> delete(@RequestBody List<Long> stockIds) {

		this.reset(stockIds);

		stockIds.forEach(stockService::deleteById);

		return stockIds;

	}

	@RequestMapping("/")
	public List<FinStock> getAll() {

		return stockService.getAll();

	}

	@RequestMapping("/light/")
	public List<LightStockDTO> getAllLight() {

		return stockService.getAll().stream() //
				.map(oneStock -> {
					return new LightStockDTO( //
							oneStock, //
							stockHistoryService.getLatestHistoryById(oneStock.id), //
							stockIndicatorService.getLatestIndicatorById(oneStock.id));
				}).collect(Collectors.toList());

	}

	@RequestMapping("/by-tag/{tag}")
	public List<FinStock> getByTag(@PathVariable(value = "tag") String tag) {
		return stockService.findByTag(tag);
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

		StockHistory stockHistory = stockHistoryService.getLatestHistory(stockCode);

		return stockHistory != null ? stockHistory : new StockHistory();

	}

	@RequestMapping("/mail")
	public void mail() throws Exception {

		schedulingService.sendEMail();

	}

	@RequestMapping("/query")
	public List<FinStock> query(@RequestParam(value = "q") String q) throws IOException {

		return stockService.findBySubstring(q);

	}

	@RequestMapping(method = RequestMethod.POST, value = "/reset-history")
	public List<LightStockDTO> reset(@RequestBody List<Long> stockIds) {

		return stockIds.stream() //
				.map(oneStockId -> {
					FinStock stock = stockService.getByStockId(oneStockId);
					if (stock == null) {
						throw new RuntimeException("Invalid stock id (" + oneStockId + ") !");
					}
					stockHistoryService.deleteByStockId(oneStockId);
					stockIndicatorService.deleteByStockId(oneStockId);

					return new LightStockDTO( //
							stock, //
							stockHistoryService.getLatestHistoryById(oneStockId), //
							stockIndicatorService.getLatestIndicatorById(oneStockId));

				}) //
				.collect(Collectors.toList());

	}

	@RequestMapping(method = RequestMethod.POST, value = "/update-history-bourso")
	public List<LightStockDTO> updateHistoryBourso(@RequestBody List<Long> stockIds) throws IOException {

		return stockIds.stream() //
				.map(oneStockId -> {
					FinStock stock = stockService.getByStockId(oneStockId);
					try {
						if (stock == null) {
							throw new Exception("Invalid stock id (" + oneStockId + ") !");
						}
						boursoService.updateOneStockHistory(stock);

					} catch (Exception e) {
						throw new RuntimeException("ERROR updateHistoryBourso", e);
					}
					return new LightStockDTO( //
							stock, //
							stockHistoryService.getLatestHistoryById(oneStockId), //
							stockIndicatorService.getLatestIndicatorById(oneStockId));

				}) //
				.collect(Collectors.toList());

	}

	@RequestMapping(method = RequestMethod.POST, value = "/update-history-yahoo")
	public List<LightStockDTO> updateHistoryYahoo(@RequestBody List<Long> stockIds) throws IOException {

		return stockIds.stream() //
				.map(oneStockId -> {
					FinStock stock = stockService.getByStockId(oneStockId);
					try {
						if (stock == null) {
							throw new Exception("Invalid stock id (" + oneStockId + ") !");
						}
						yahooService.updateOneStockHistory(stock, false);

					} catch (Exception e) {
						throw new RuntimeException("ERROR updateHistoryYahoo", e);
					}
					return new LightStockDTO( //
							stock, //
							stockHistoryService.getLatestHistoryById(oneStockId), //
							stockIndicatorService.getLatestIndicatorById(oneStockId));

				}) //
				.collect(Collectors.toList());

	}

	@RequestMapping(method = RequestMethod.POST, value = "/update-indicators")
	public List<LightStockDTO> updateIndicators(@RequestBody List<Long> stockIds) throws IOException {

		return stockIds.stream() //
				.map(oneStockId -> {
					FinStock stock = stockService.getByStockId(oneStockId);
					if (stock == null) {
						throw new RuntimeException("Invalid stock id (" + oneStockId + ") !");
					}
					stockIndicatorService.updateIndicators(stock, false);

					return new LightStockDTO( //
							stock, //
							stockHistoryService.getLatestHistoryById(oneStockId), //
							stockIndicatorService.getLatestIndicatorById(oneStockId));

				}) //
				.collect(Collectors.toList());

	}

}
