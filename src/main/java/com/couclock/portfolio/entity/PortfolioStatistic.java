package com.couclock.portfolio.entity;

import javax.persistence.Embeddable;

@Embeddable
public class PortfolioStatistic {

	public String stockCode;
	public long dayCount = 0;
	public double performance = 0;

	@Override
	public String toString() {
		return String.format("PortfolioStatistic [stockCode=%s, dayCount=%s, performance=%s]", stockCode, dayCount,
				performance);
	}

}
