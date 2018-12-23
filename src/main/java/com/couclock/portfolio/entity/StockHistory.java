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
public class StockHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stock_code", referencedColumnName = "code")
	@JsonIgnore
	public FinStock stock;

	public LocalDate date;
	public double open;
	public double close;
	public double low;
	public double high;

	public long volume;

	@Override
	public String toString() {
		return String.format("StockHistory [id=%s, stock=%s, date=%s, open=%s, close=%s, low=%s, high=%s, volume=%s]",
				id, stock.code, date, open, close, low, high, volume);
	}

}
