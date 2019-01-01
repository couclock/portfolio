package com.couclock.portfolio.entity;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class StockIndicator {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stock_code", referencedColumnName = "code")
	@JsonIgnore
	public FinStock stock;

	public LocalDate date;

	public double perf1Month;
	public double perf3Months;
	public double perf6Months;

	public double ema6Months;

	@Override
	public String toString() {
		return String.format("StockIndicator [id=%s, stock=%s, date=%s, perf1Month=%s, perf3Months=%s, perf6Months=%s]",
				id, stock, date, perf1Month, perf3Months, perf6Months);
	}

}
