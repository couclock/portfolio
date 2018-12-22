package com.couclock.portfolio.rest;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.couclock.portfolio.entity.Portfolio;
import com.couclock.portfolio.entity.PortfolioHistory;
import com.couclock.portfolio.entity.PortfolioStatus;
import com.couclock.portfolio.service.PortfolioService;
import com.couclock.portfolio.service.StrategyService;

@RestController
@RequestMapping("/strategies")
@CrossOrigin(origins = "*")
public class StrategiesController {

	@Autowired
	private StrategyService strategyService;
	@Autowired
	private PortfolioService portfolioService;

	@RequestMapping(method = RequestMethod.POST, value = "/{strategyCode}/{usStockCode}/{exUsStockCode}/{bondStockCode}")
	public String addStrategy(@PathVariable(value = "strategyCode") String strategyCode,
			@PathVariable(value = "usStockCode") String usStockCode,
			@PathVariable(value = "exUsStockCode") String exUsStockCode,
			@PathVariable(value = "bondStockCode") String bondStockCode) throws Exception {

		Portfolio portfolio = portfolioService.getByStrategyCode(strategyCode);

		if (portfolio != null) {
			throw new Exception("that strategyCode already exists !");
		} else {
			portfolio = new Portfolio();
			portfolio.strategyCode = strategyCode;
			portfolio.startDate = LocalDate.parse("2005-01-01");
			portfolio.startMoney = 10000;
			portfolio.addAddMoneyEvent(portfolio.startDate, portfolio.startMoney);
			portfolio.endStatus = new PortfolioStatus();
			portfolio.endStatus.money = portfolio.startMoney;
			portfolio.endDate = portfolio.startDate;
			portfolio.usStockCode = usStockCode;
			portfolio.exUsStockCode = exUsStockCode;
			portfolio.bondStockCode = bondStockCode;

			portfolioService.upsert(portfolio);

		}

		return "ok";

	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/{strategyCode}")
	public String deleteOne(@PathVariable(value = "strategyCode") String strategyCode) {

		portfolioService.deleteByStrategyCode(strategyCode);

		return "ok";

	}

	@RequestMapping("/{strategyCode}")
	public Portfolio get(@PathVariable(value = "strategyCode") String strategyCode) {

		return portfolioService.getByStrategyCode(strategyCode);

	}

	@RequestMapping("/")
	public List<Portfolio> getAll() {

		return portfolioService.getAll();

	}

	@RequestMapping("/{strategyCode}/history")
	public List<PortfolioHistory> getHistory(@PathVariable(value = "strategyCode") String strategyCode) {
		return portfolioService.getByStrategyCode(strategyCode).history;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{strategyCode}/process")
	public Portfolio processStrategy(@PathVariable(value = "strategyCode") String strategyCode) throws Exception {

		Portfolio portfolio = portfolioService.getByStrategyCode(strategyCode);

		if (portfolio != null) {
			portfolio = strategyService.acceleratedDualMomentum(portfolio);
		} else {
			portfolio = new Portfolio();

			portfolio.strategyCode = strategyCode;

			portfolio = portfolioService.initPortfolio(portfolio);

			portfolio = strategyService.acceleratedDualMomentum(portfolio);

		}

		portfolioService.upsert(portfolio);

		return portfolio;

	}

	@RequestMapping(method = RequestMethod.POST, value = "/{strategyCode}/reset")
	public Portfolio resetStrategy(@PathVariable(value = "strategyCode") String strategyCode) throws Exception {

		Portfolio portfolio = portfolioService.getByStrategyCode(strategyCode);

		if (portfolio == null) {
			throw new Exception("Invalid strategy code !");
		}

		portfolio = portfolioService.initPortfolio(portfolio);

		portfolioService.upsert(portfolio);

		return portfolio;

	}

}
