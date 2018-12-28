package com.couclock.portfolio.entity;

import javax.persistence.Embeddable;

@Embeddable
public class StockDistribution {

	public double percent;
	public String stockCode;

	public StockDistribution() {

	}

	public StockDistribution(double percent, String stockCode) {
		this.percent = percent;
		this.stockCode = stockCode;
	}

	@Override
	public String toString() {
		return String.format("StockDistribution [percent=%s, stockCode=%s]", percent, stockCode);
	}

}
