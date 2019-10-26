package com.couclock.portfolio.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.couclock.portfolio.entity.Backtest;
import com.couclock.portfolio.entity.BacktestTransaction;
import com.couclock.portfolio.entity.FinStock;
import com.couclock.portfolio.entity.StockHistory;
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
	protected StockHistoryService stockHistoryService;
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

	public Backtest updateBacktestLabel(long backtestId, String backtestLabel) throws Exception {

		Optional<Backtest> check = backtestRepository.findById(backtestId);
		if (!check.isPresent()) {
			throw new EntityNotFoundException("Backtest " + backtestId + " not found !");
		}

		Backtest bt = check.get();
		bt.label = backtestLabel;

		bt = backtestRepository.save(bt);

		return bt;

	}

	/**
	 * Retourne l'historique du backtest sous la forme attendue par la lib de graph
	 * côté front (highcharts) Format attendu : [[timestamp1, val1], [timestamp2,
	 * val2] ...]
	 * 
	 * @param backtestId
	 * @return
	 */
	public List<List<Number>> getBacktestHistory(long backtestId) {

		Optional<Backtest> check = backtestRepository.findById(backtestId);
		if (!check.isPresent()) {
			throw new EntityNotFoundException("Backtest " + backtestId + " not found !");
		}

		List<List<Number>> result = new ArrayList<>();

		Backtest bt = check.get();
		List<BacktestTransaction> transactions = bt.transactions.stream() //
				.sorted((t1, t2) -> t1.buyDate.compareTo(t2.buyDate)) //
				.collect(Collectors.toList());

		Double currentValue = bt.startMoney;
		Double cashMoney = bt.startMoney;
		BacktestTransaction currentTx = null;

		LocalDate current = bt.startDate;
		while (!current.isAfter(bt.endDate)) {

			if (currentTx != null) {
				if (current.equals(currentTx.sellDate)) {
					currentValue = cashMoney + currentTx.sellValue * currentTx.quantity;
					cashMoney = currentValue;
				} else {
					currentValue = cashMoney + getStockCloseValue(currentTx.stock, current) * currentTx.quantity;
				}
			}

			Optional<BacktestTransaction> startingTx = getTransactionStartingAtDate(transactions, current);
			if (startingTx.isPresent()) {
				currentTx = startingTx.get();
				cashMoney = cashMoney - currentTx.buyValue * currentTx.quantity;
				currentValue = cashMoney + getStockCloseValue(currentTx.stock, current) * currentTx.quantity;
			}

			result.add(Arrays.asList(current.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
					currentValue));
			current = current.plusDays(1);
		}

		return result;

	}

	private double getStockCloseValue(FinStock stock, LocalDate date) {
		StockHistory stockHistory = stockHistoryService.findFirstHistoryAfter(stock.code, date, date.plusMonths(1));
		if (stockHistory == null) {
			return 0;
		}

		return stockHistory.close;
	}

	private Optional<BacktestTransaction> getTransactionStartingAtDate(List<BacktestTransaction> transactions,
			LocalDate date) {
		Optional<BacktestTransaction> result = transactions.stream() //
				.filter(oneTransaction -> {
					if (date.equals(oneTransaction.buyDate)) {
						return true;
					}
					return false;
				}).findFirst();

		return result;
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
