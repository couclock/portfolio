package com.couclock.portfolio.entity.strategies;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;

import com.couclock.portfolio.entity.StockDistribution;

@Entity
public class AcceleratedMomentumStrategy extends StrategyParameters {

	@ElementCollection
	public List<StockDistribution> usStocks = new ArrayList<>();
	@ElementCollection
	public List<StockDistribution> exUsStocks = new ArrayList<>();
	@ElementCollection
	public List<StockDistribution> bondStocks = new ArrayList<>();

	public double ema6MonthsProtectionRatio;

	public STRATEGY strategy = STRATEGY.ACCELERATED_MOMENTUM;

	public AcceleratedMomentumStrategy() {
		this.strategy = STRATEGY.ACCELERATED_MOMENTUM;
	}

	public void addBondStock(double percent, String stockCode) {
		this.bondStocks.add(new StockDistribution(percent, stockCode));
	}

	public void addExUsStock(double percent, String stockCode) {
		this.exUsStocks.add(new StockDistribution(percent, stockCode));
	}

	public void addUsStock(double percent, String stockCode) {
		this.usStocks.add(new StockDistribution(percent, stockCode));
	}

	@Override
	public List<String> getStockList() {
		return Stream.concat( //
				Stream.concat(this.usStocks.stream(), this.exUsStocks.stream()), //
				this.bondStocks.stream()) //
				.map(oneStock -> oneStock.stockCode) //
				.collect(Collectors.toList());
	}

}
