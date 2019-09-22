package com.couclock.portfolio.service;

import java.util.ArrayList;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.couclock.portfolio.entity.Backtest;
import com.couclock.portfolio.entity.strategies.StrategyParameters.STRATEGY;
import com.couclock.portfolio.repository.BacktestRepository;
import com.couclock.portfolio.service.strategies.AcceleratedMomentumStrategy;

/**
 *
 * @author dany
 *
 */
@Service
public class BacktestService {

	private static final Logger log = LoggerFactory.getLogger(BacktestService.class);

	@Autowired
	private BacktestRepository backtestRepository;

	@Autowired
	private AcceleratedMomentumStrategy acceleratedMomentumStrategy;

	public Backtest continueBacktest(long backtestId) throws Exception {

		Optional<Backtest> check = backtestRepository.findById(backtestId);
		if (!check.isPresent()) {
			throw new EntityNotFoundException("Backtest " + backtestId + " not found !");
		}

		Backtest bt = check.get();

		if (bt.strategyCode.equals(STRATEGY.ACCELERATED_MOMENTUM)) {
			acceleratedMomentumStrategy.continueBacktest(bt);
		}

		return bt;

	}

	public Backtest getBacktestHistory(long backtestId) {

		Optional<Backtest> check = backtestRepository.findById(backtestId);
		if (!check.isPresent()) {
			throw new EntityNotFoundException("Backtest " + backtestId + " not found !");
		}

		Backtest bt = check.get();

		return bt;

	}

	public Backtest resetBacktest(long backtestId) {

		Optional<Backtest> check = backtestRepository.findById(backtestId);
		if (!check.isPresent()) {
			throw new EntityNotFoundException("Backtest " + backtestId + " not found !");
		}

		Backtest bt = check.get();
		bt.endDate = null;
		bt.currentMoney = bt.startMoney;
		bt.transactions = new ArrayList<>();

		backtestRepository.save(bt);

		return bt;

	}

	public Backtest upsert(Backtest backtest) throws Exception {

		backtestRepository.save(backtest);

		return backtest;

	}

}
