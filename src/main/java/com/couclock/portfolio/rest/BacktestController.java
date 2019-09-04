package com.couclock.portfolio.rest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.couclock.portfolio.entity.Backtest;
import com.couclock.portfolio.entity.strategies.Strategy;
import com.couclock.portfolio.repository.BacktestRepository;
import com.couclock.portfolio.service.BacktestService;
import com.couclock.portfolio.service.strategies.AcceleratedMomentumStrategy;

@RestController
@RequestMapping("/backtests")
@CrossOrigin(origins = "*")
public class BacktestController {

	@Autowired
	private BacktestRepository backtestRepository;
	@Autowired
	private BacktestService backtestService;

	@RequestMapping(value = "/{backtestId}/continue", method = RequestMethod.POST)
	public Backtest continueBacktest(@PathVariable(value = "backtestId") Long backtestId) throws Exception {

		return backtestService.continueBacktest(backtestId);

	}

	@RequestMapping(value = "/{backtestId}", method = RequestMethod.DELETE)
	public String delete(@PathVariable(value = "backtestId") Long backtestId) {

		Optional<Backtest> check = backtestRepository.findById(backtestId);
		if (check.isPresent()) {
			backtestRepository.delete(check.get());
		}

		return "ok";

	}

	@RequestMapping("/")
	public List<Backtest> getAll() throws Exception {

		return backtestRepository.findAll();

	}

	@RequestMapping(value = "/{backtestId}", method = RequestMethod.GET)
	public Backtest getBacktest(@PathVariable(value = "backtestId") Long backtestId) throws Exception {

		Optional<Backtest> check = backtestRepository.findById(backtestId);
		if (check.isPresent()) {
			return check.get();
		}
		return null;

	}

	@RequestMapping("/init")
	public String init() throws Exception {

		Backtest bt = new Backtest();
		bt.startMoney = 10000;
		bt.currentMoney = 10000;
		bt.startDate = LocalDate.parse("2016-01-01");
		bt.strategyCode = Strategy.ACCELERATED_MOMENTUM;
		bt.strategyParameters = new HashMap<>();
		bt.strategyParameters.put(AcceleratedMomentumStrategy.US_STOCK, "500");
		bt.strategyParameters.put(AcceleratedMomentumStrategy.EX_US_STOCK, "MMS");
		bt.strategyParameters.put(AcceleratedMomentumStrategy.BOND_STOCK, "EVOE");

//		BacktestTransaction btt = new BacktestTransaction();
//		btt.buyDate = LocalDate.parse("2018-02-06");
//		btt.quantity = 12;
//		btt.stock = stockRepository.findByCodeIgnoreCase("500");
//		bt.transactions.add(btt);

		backtestRepository.save(bt);

		return "ok";

	}

	@RequestMapping(value = "/{backtestId}/reset", method = RequestMethod.POST)
	public Backtest resetBacktest(@PathVariable(value = "backtestId") Long backtestId) throws Exception {

		return backtestService.resetBacktest(backtestId);

	}

	@RequestMapping(value = "/{backtestId}/run", method = RequestMethod.POST)
	public Backtest runBacktest(@PathVariable(value = "backtestId") Long backtestId) throws Exception {

		backtestService.resetBacktest(backtestId);
		return backtestService.continueBacktest(backtestId);

	}

}
