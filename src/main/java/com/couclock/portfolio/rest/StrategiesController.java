package com.couclock.portfolio.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.couclock.portfolio.service.StrategyService;

@RestController
@RequestMapping("/strategies")
public class StrategiesController {

	@Autowired
	private StrategyService strategyService;

	@RequestMapping("/{strategyCode}")
	public String get(@PathVariable(value = "strategyCode") String strategyCode) throws Exception {

		strategyService.acceleratedDualMomentum();

		return "OK";

	}

}
