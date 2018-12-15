package com.couclock.portfolio.entity.sub;

import java.time.LocalDate;

import javax.persistence.Entity;

@Entity
public class PortfolioSellEvent extends PortfolioEvent {

	public long count;
	public String stockCode;

	public PortfolioSellEvent() {
	}

	public PortfolioSellEvent(LocalDate date, long count, String stockCode) {
		this.date = date;
		this.type = EVENT_TYPE.SELL;
		this.count = count;
		this.stockCode = stockCode;
	}

	@Override
	public String toString() {
		return String.format("PortfolioSellEventDTO [count=%s, stockCode=%s, date=%s, type=%s]", count, stockCode, date,
				type);
	}

}
