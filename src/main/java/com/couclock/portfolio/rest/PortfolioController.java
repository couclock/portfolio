package com.couclock.portfolio.rest;

import java.time.LocalDate;
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
import com.couclock.portfolio.entity.PortfolioStatus;
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
			portfolio.startDate = LocalDate.parse("2010-01-01");
			portfolio.startMoney = portfolio.endMoney = 10000;
			portfolio.addAddMoneyEvent(portfolio.startDate, portfolio.startMoney);
			portfolio.currentStatus = new PortfolioStatus();
			portfolio.currentStatus.money = portfolio.startMoney;
			portfolio.endDate = portfolio.startDate;
			portfolio.strategyParameters = newPortfolio.strategyParameters;

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
			portfolio.startDate = LocalDate.parse("2010-01-01");
			portfolio.startMoney = portfolio.endMoney = 10000;
			portfolio.addAddMoneyEvent(portfolio.startDate, portfolio.startMoney);
			portfolio.currentStatus = new PortfolioStatus();
			portfolio.currentStatus.money = portfolio.startMoney;
			portfolio.endDate = portfolio.startDate;
			AcceleratedMomentumStrategy strategyParameters = new AcceleratedMomentumStrategy();
			strategyParameters.addUsStock(1, usStockCode);
			strategyParameters.addExUsStock(1, exUsStockCode);
			strategyParameters.addBondStock(1, bondStockCode);
			portfolio.strategyParameters = strategyParameters;

			portfolioService.upsert(portfolio);

		}

		return "ok";

	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/{portfolioCode}")
	public String deleteOne(@PathVariable(value = "portfolioCode") String portfolioCode) {

		portfolioService.deleteByPortfolioCode(portfolioCode);

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

	@RequestMapping(method = RequestMethod.POST, value = "/{portfolioCode}/process")
	public Portfolio processBacktest(@PathVariable(value = "portfolioCode") String portfolioCode) throws Exception {

		Portfolio portfolio = portfolioService.getByPortfolioCode(portfolioCode);

		if (portfolio != null) {
			portfolio = strategyService.acceleratedDualMomentum(portfolio);
		} else {
			portfolio = new Portfolio();

			portfolio.code = portfolioCode;

			portfolio = portfolioService.initPortfolio(portfolio);

			portfolio = strategyService.acceleratedDualMomentum(portfolio);

		}

		portfolioService.upsert(portfolio);

		return portfolio;

	}

	@RequestMapping(method = RequestMethod.POST, value = "/{portfolioCode}/reset")
	public Portfolio resetBacktest(@PathVariable(value = "portfolioCode") String portfolioCode) throws Exception {

		Portfolio portfolio = portfolioService.getByPortfolioCode(portfolioCode);

		if (portfolio == null) {
			throw new Exception("Invalid strategy code !");
		}

		portfolio = portfolioService.initPortfolio(portfolio);

		portfolioService.upsert(portfolio);

		return portfolio;

	}

}
