package com.couclock.portfolio.service.strategies;

import java.time.LocalDate;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.couclock.portfolio.entity.Backtest;
import com.couclock.portfolio.entity.BacktestTransaction;
import com.couclock.portfolio.entity.FinStock;
import com.couclock.portfolio.entity.StockHistory;
import com.couclock.portfolio.entity.StockIndicator;
import com.couclock.portfolio.entity.strategies.AcceleratedMomentumParameters;
import com.couclock.portfolio.entity.strategies.StrategyParameters;
import com.couclock.portfolio.service.BacktestService;
import com.couclock.portfolio.service.StockIndicatorService;
import com.couclock.portfolio.service.StockService;
import com.couclock.portfolio.utils.DateUtils;

@Service
public class AcceleratedMomentumStrategy extends AbstractStrategy {
	private static final Logger log = LoggerFactory.getLogger(AcceleratedMomentumStrategy.class);

	public final static String US_STOCK = "usStock";
	public final static String EX_US_STOCK = "exUsStock";
	public final static String BOND_STOCK = "bondStock";

	@Autowired
	private DateUtils dateUtils;

	@Autowired
	private StockService stockService;
	@Autowired
	private BacktestService backtestService;

	@Autowired
	private StockIndicatorService stockIndicatorService;

	@Override
	public void continueBacktest(Backtest bt) throws Exception {

		checkParameters(bt.strategyParameters);
		AcceleratedMomentumParameters amParam = (AcceleratedMomentumParameters) bt.strategyParameters;

		LocalDate currentDate = LocalDate.from(bt.startDate);
		Optional<BacktestTransaction> openTransactionOpt = bt.transactions.stream() //
				.filter(oneBt -> oneBt.sellDate == null) //
				.findFirst();
		FinStock nextStock = null;
		BacktestTransaction openTransaction = null;

		if (openTransactionOpt.isPresent()) {
			openTransaction = openTransactionOpt.get();
			currentDate = openTransaction.buyDate;
		}

		while (currentDate.isBefore(LocalDate.now())) {
			currentDate = dateUtils.getLastDayOfMonth(currentDate);
			if (currentDate.isAfter(LocalDate.now())) {
				break;
			}
			nextStock = getNextStock(currentDate, amParam);

			if (openTransaction != null && !openTransaction.stock.code.equals(nextStock.code)) {
				openTransaction = sellStock(currentDate.plusDays(1), openTransaction);
				bt.currentMoney = bt.currentMoney + openTransaction.quantity * openTransaction.sellValue;
				currentDate = openTransaction.sellDate;
			} else {
				currentDate = currentDate.plusDays(1);
			}
			if (openTransaction == null || !openTransaction.stock.code.equals(nextStock.code)) {
				openTransaction = buyStock(currentDate, nextStock, bt.currentMoney);
				if (openTransaction != null) {
					bt.currentMoney = bt.currentMoney - openTransaction.quantity * openTransaction.buyValue;
					bt.transactions.add(openTransaction);
					currentDate = openTransaction.buyDate;
				}
			}

		}

		bt.estimatedValue = bt.currentMoney + getEstimatedTransactionCurrentValue(openTransaction);
		bt.endDate = LocalDate.now();
		bt.cagr = getCAGR(bt);

		backtestService.upsert(bt);

	}

	private BacktestTransaction buyStock(LocalDate date, FinStock stock, double money) {
		StockHistory stockHistory = stockHistoryService.findFirstHistoryAfter(stock.code, date, date.plusMonths(1));
		if (stockHistory == null) {
			return null;
		}

		BacktestTransaction btt = new BacktestTransaction();
		btt.buyDate = stockHistory.date;
		btt.buyValue = stockHistory.open;
		btt.quantity = (int) (money / btt.buyValue);
		btt.stock = stock;
		return btt;
	}

	private void checkParameters(StrategyParameters parameters) throws Exception {

		if (parameters instanceof AcceleratedMomentumParameters) {
			AcceleratedMomentumParameters amParam = (AcceleratedMomentumParameters) parameters;
			if (amParam.usStock == null) {
				throw new Exception("Back parameter must contain a valid usStock");
			}
			if (amParam.exUsStock == null) {
				throw new Exception("Back parameter must contain a valid exusStock");
			}
			if (amParam.bondStock == null) {
				throw new Exception("Back parameter must contain a valid bondStock");
			}

		} else {
			throw new Exception("Wrong strategy parameter type : AcceleratedMomentumParameters required !");
		}

	}

	private FinStock getNextStock(LocalDate lastDayOfMonth, AcceleratedMomentumParameters parameters) {

		FinStock usStock = stockService.getByCode(parameters.usStock);
		FinStock exUsStock = stockService.getByCode(parameters.exUsStock);
		FinStock bondStock = stockService.getByCode(parameters.bondStock);

		StockIndicator usStockIndicator = stockIndicatorService.findFirstIndicatorBefore(usStock.code, lastDayOfMonth,
				lastDayOfMonth.minusMonths(1));
		StockIndicator exUsStockIndicator = stockIndicatorService.findFirstIndicatorBefore(exUsStock.code,
				lastDayOfMonth, lastDayOfMonth.minusMonths(1));

		double momentumExUS = 0;
		if (exUsStockIndicator != null) {
			momentumExUS = exUsStockIndicator.perf1Month + exUsStockIndicator.perf3Months
					+ exUsStockIndicator.perf6Months;
		}
		double momentumUS = 0;
		if (usStockIndicator != null) {
			momentumUS = usStockIndicator.perf1Month + usStockIndicator.perf3Months + usStockIndicator.perf6Months;
		}

		FinStock targetStock = null;
		if (momentumUS > momentumExUS) {
			if (momentumUS > 0) {
				targetStock = usStock;
			} else {
				targetStock = bondStock;
			}
		} else {
			if (momentumExUS > 0) {
				targetStock = exUsStock;
			} else {
				targetStock = bondStock;
			}
		}

		return targetStock;

	}

	private BacktestTransaction sellStock(LocalDate date, BacktestTransaction btt) {
		StockHistory stockHistory = stockHistoryService.findFirstHistoryAfter(btt.stock.code, date, null);
		if (stockHistory == null) {
			log.warn("Cannot sell : history after " + date + " for " + btt.stock.code + " cannot be found.");
			return null;
		}

		btt.sellDate = stockHistory.date;
		btt.sellValue = stockHistory.open;
		return btt;
	}

}
