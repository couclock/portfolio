package com.couclock.portfolio.service.strategies;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;

import com.couclock.portfolio.entity.Backtest;
import com.couclock.portfolio.entity.BacktestTransaction;
import com.couclock.portfolio.entity.StockHistory;
import com.couclock.portfolio.service.StockHistoryService;

public abstract class AbstractStrategy {

	@Autowired
	protected StockHistoryService stockHistoryService;

	public abstract void continueBacktest(Backtest bt) throws Exception;

	protected double getCAGR(Backtest bt) {

		double years = ChronoUnit.DAYS.between(bt.startDate, bt.endDate) * 1.0;
		years = years / 365.0;
		years = 1 / years;

		double cagr = bt.estimatedValue / bt.startMoney;
		cagr = Math.pow(cagr, years) - 1;

		return cagr;
	}

	protected double getEstimatedTransactionCurrentValue(BacktestTransaction bt) {
		if (bt.sellDate != null) {
			return 0;
		}
		StockHistory stockHistory = stockHistoryService.findFirstHistoryBefore(bt.stock.code, LocalDate.now(), null);
		if (stockHistory == null) {
			return 0;
		}
		return stockHistory.close * bt.quantity;
	}

}
