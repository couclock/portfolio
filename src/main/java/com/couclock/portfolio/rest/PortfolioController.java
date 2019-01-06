package com.couclock.portfolio.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.couclock.portfolio.entity.Portfolio;
import com.couclock.portfolio.entity.PortfolioHistory;
import com.couclock.portfolio.entity.strategies.AcceleratedMomentumStrategy;
import com.couclock.portfolio.entity.sub.PortfolioEvent;
import com.couclock.portfolio.service.PortfolioService;
import com.couclock.portfolio.service.StrategyService;

@RestController
@RequestMapping("/portfolios")
@CrossOrigin(origins = "*")
public class PortfolioController {

	@Autowired
	private StrategyService strategyService;

	@Autowired
	private PortfolioService portfolioService;

	@RequestMapping(method = RequestMethod.POST)
	public String addPortfolio(@RequestBody Portfolio newPortfolio) throws Exception {

		Portfolio portfolio = portfolioService.getByPortfolioCode(newPortfolio.code);

		if (portfolio != null) {
			throw new Exception("That portfolio code already exists !");
		} else {
			portfolio = new Portfolio();
			portfolio.code = newPortfolio.code;
			portfolio.strategyParameters = newPortfolio.strategyParameters;
			portfolio.strategyParameters.portfolio = portfolio;

			portfolioService.initPortfolio(portfolio);

			portfolioService.upsert(portfolio);

		}

		return "ok";

	}

	@RequestMapping(method = RequestMethod.POST, value = "/{portfolioCode}/{usStockCode}/{exUsStockCode}/{bondStockCode}")
	public String addPortfolio(@PathVariable(value = "portfolioCode") String portfolioCode,
			@PathVariable(value = "usStockCode") String usStockCode,
			@PathVariable(value = "exUsStockCode") String exUsStockCode,
			@PathVariable(value = "bondStockCode") String bondStockCode) throws Exception {

		Portfolio portfolio = portfolioService.getByPortfolioCode(portfolioCode);

		if (portfolio != null) {
			throw new Exception("That portfolioCode already exists !");
		} else {
			portfolio = new Portfolio();
			portfolio.code = portfolioCode;

			portfolioService.initPortfolio(portfolio);

			AcceleratedMomentumStrategy strategyParameters = new AcceleratedMomentumStrategy();
			strategyParameters.addUsStock(1, usStockCode);
			strategyParameters.addExUsStock(1, exUsStockCode);
			strategyParameters.addBondStock(1, bondStockCode);
			portfolio.strategyParameters = strategyParameters;

			portfolioService.upsert(portfolio);

		}

		return "ok";

	}

	@RequestMapping(method = RequestMethod.DELETE)
	public String delete(@RequestBody List<Long> idList) {

		idList.forEach(onePortfolioId -> {
			portfolioService.deleteById(onePortfolioId);
		});

		return "ok";

	}

	@RequestMapping("/generate")
	public String generate() throws Exception {

		portfolioService.generatePortfolios();
		return "ok";

	}

	@RequestMapping("/")
	public List<Portfolio> getAll() {

		return portfolioService.getAll();

	}

	@RequestMapping("/{portfolioCode}/events")
	public List<PortfolioEvent> getEvents(@PathVariable(value = "portfolioCode") String portfolioCode) {
		return portfolioService.getEventsByPortfolioCode(portfolioCode);
	}

	@RequestMapping("/{portfolioCode}/history")
	public List<PortfolioHistory> getHistory(@PathVariable(value = "portfolioCode") String portfolioCode) {
		return portfolioService.getByPortfolioCode(portfolioCode).history;
	}

	@RequestMapping("/{portfolioCode}")
	public Portfolio getOne(@PathVariable(value = "portfolioCode") String portfolioCode) {

		return portfolioService.getByPortfolioCode(portfolioCode);

	}

	@RequestMapping(method = RequestMethod.POST, value = "/process-backtest")
	public List<Portfolio> processBacktest(@RequestBody List<Long> idList) throws Exception {

		List<Portfolio> result = new ArrayList<>();

		idList.forEach(onePortfolioId -> {
			Portfolio portfolio = portfolioService.getByPortfolioId(onePortfolioId);
			try {
				if (portfolio == null) {
					throw new Exception("Invalid portfolio id (" + onePortfolioId + ") !");
				}
				portfolio = strategyService.acceleratedDualMomentum(portfolio);
				portfolio = portfolioService.upsert(portfolio);
			} catch (Exception e) {
				throw new RuntimeException("ERROR processBacktest", e);
			}

			result.add(portfolio);
		});

		return result;

	}

	@RequestMapping(method = RequestMethod.POST, value = "/reset-backtest")
	public List<Portfolio> resetBacktest(@RequestBody List<Long> idList) throws Exception {

		List<Portfolio> result = new ArrayList<>();

		idList.forEach(onePortfolioId -> {
			Portfolio portfolio = portfolioService.getByPortfolioId(onePortfolioId);
			try {
				if (portfolio == null) {
					throw new Exception("Invalid portfolio id (" + onePortfolioId + ") !");
				}
				portfolio = portfolioService.initPortfolio(portfolio);
				portfolio = portfolioService.upsert(portfolio);
			} catch (Exception e) {
				throw new RuntimeException("ERROR resetBacktest", e);
			}

			result.add(portfolio);
		});

		return result;

	}

}
