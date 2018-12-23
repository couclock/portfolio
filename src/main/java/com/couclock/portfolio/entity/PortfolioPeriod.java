package com.couclock.portfolio.entity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import javax.persistence.Embeddable;

@Embeddable
public class PortfolioPeriod {

	public String stockCode;
	public LocalDate startDate;
	public LocalDate endDate;
	public double buyPrice;
	public double sellPrice;
	public long count;
	public double perf;
	public long duration;

	public void processDuration() {
		if (this.startDate != null && this.endDate != null) {
			this.duration = ChronoUnit.DAYS.between(this.startDate, this.endDate);
		}
	}

	public void processPerf() {
		if (this.buyPrice != 0) {
			this.perf = (this.sellPrice - this.buyPrice) / this.buyPrice;
		}
	}

	@Override
	public String toString() {
		return String.format(
				"PortfolioPeriod [stockCode=%s, startDate=%s, endDate=%s, buyPrice=%s, sellPrice=%s, count=%s, perf=%s, duration=%s]",
				stockCode, startDate, endDate, buyPrice, sellPrice, count, perf, duration);
	}

}
