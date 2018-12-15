package com.couclock.portfolio.entity.sub;

import java.time.LocalDate;

import javax.persistence.Entity;

@Entity
public class PortfolioBuyEvent extends PortfolioEvent {

	public long count;
	public String stockCode;

	public PortfolioBuyEvent() {
	}

	public PortfolioBuyEvent(LocalDate date, long count, String stockCode) {
		this.date = date;
		this.type = EVENT_TYPE.BUY;
		this.count = count;
		this.stockCode = stockCode;
	}

	@Override
	public String toString() {
		return String.format("PortfolioBuyEventDTO [count=%s, stockCode=%s, date=%s, type=%s]", count, stockCode, date,
				type);
	}

}
