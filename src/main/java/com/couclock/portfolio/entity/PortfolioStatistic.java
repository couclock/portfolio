package com.couclock.portfolio.entity;

import javax.persistence.Embeddable;

@Embeddable
public class PortfolioStatistic {

	public long dayCount = 0;
	public double performance = 0;

}
