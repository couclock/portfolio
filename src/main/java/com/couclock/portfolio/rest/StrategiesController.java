package com.couclock.portfolio.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.couclock.portfolio.entity.Portfolio;
import com.couclock.portfolio.service.PortfolioService;
import com.couclock.portfolio.service.StrategyService;

@RestController
@RequestMapping("/strategies")
public class StrategiesController {

	@Autowired
	private StrategyService strategyService;
	@Autowired
	private PortfolioService portfolioService;

	@RequestMapping("/{strategyCode}")
	public Portfolio get(@PathVariable(value = "strategyCode") String strategyCode) throws Exception {

		return portfolioService.getByStrategyCode(strategyCode);

	}

	@RequestMapping(method = RequestMethod.POST, value = "/{strategyCode}/process")
	public Portfolio processStrategy(@PathVariable(value = "strategyCode") String strategyCode) throws Exception {

		Portfolio portfolio = strategyService.acceleratedDualMomentum("500", "MMS", "USTY");

		portfolioService.upsert(strategyCode, portfolio);

		return portfolioService.getByStrategyCode(strategyCode);

	}

}
