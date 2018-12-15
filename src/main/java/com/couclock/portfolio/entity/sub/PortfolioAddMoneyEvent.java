package com.couclock.portfolio.entity.sub;

import java.time.LocalDate;

import javax.persistence.Entity;

@Entity
public class PortfolioAddMoneyEvent extends PortfolioEvent {

	public double amount;

	public PortfolioAddMoneyEvent() {
	}

	public PortfolioAddMoneyEvent(LocalDate date, double amount) {
		this.date = date;
		this.type = EVENT_TYPE.ADD_MONEY;
		this.amount = amount;
	}

	@Override
	public String toString() {
		return String.format("PortfolioAddMoneyEventDTO [amount=%s, date=%s, type=%s]", amount, date, type);
	}

}
