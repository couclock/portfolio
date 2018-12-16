package com.couclock.portfolio.rest;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.couclock.portfolio.entity.Portfolio;
import com.couclock.portfolio.entity.PortfolioStatus;
import com.couclock.portfolio.service.PortfolioService;
import com.couclock.portfolio.service.StrategyService;

@RestController
@RequestMapping("/strategies")
public class StrategiesController {

	@Autowired
	private StrategyService strategyService;
	@Autowired
	private PortfolioService portfolioService;

	@RequestMapping(method = RequestMethod.DELETE, value = "/{strategyCode}")
	public void deleteOne(@PathVariable(value = "strategyCode") String strategyCode) throws Exception {

		portfolioService.deleteByStrategyCode(strategyCode);

	}

	@RequestMapping("/{strategyCode}")
	public Portfolio get(@PathVariable(value = "strategyCode") String strategyCode) throws Exception {

		return portfolioService.getByStrategyCode(strategyCode);

	}

	@RequestMapping(method = RequestMethod.POST, value = "/{strategyCode}/process")
	public Portfolio processStrategy(@PathVariable(value = "strategyCode") String strategyCode) throws Exception {

		Portfolio portfolio = portfolioService.getByStrategyCode(strategyCode);

		if (portfolio != null) {
			portfolio = strategyService.acceleratedDualMomentum(portfolio, "500", "MMS", "USTY");
		} else {
			portfolio = new Portfolio();
			portfolio.strategyCode = strategyCode;
			portfolio.startDate = LocalDate.parse("2005-01-01");
			portfolio.startMoney = 10000;
			portfolio.addAddMoneyEvent(portfolio.startDate, portfolio.startMoney);
			portfolio.endStatus = new PortfolioStatus();
			portfolio.endStatus.money = portfolio.startMoney;
			portfolio.endDate = portfolio.startDate;

			portfolio = strategyService.acceleratedDualMomentum(portfolio, "500", "MMS", "USTY");

		}

		portfolioService.upsert(portfolio);

		return portfolio;

	}

}
