package com.couclock.portfolio.entity;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Embeddable;
import javax.persistence.Index;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Embeddable
@Table(indexes = { @Index(name = "BUYDATE_IDX", columnList = "buyDate") })
public class BacktestTransaction implements Serializable {

	private static final long serialVersionUID = 215161339820055103L;

	public LocalDate buyDate;
	public double buyValue;

	public LocalDate sellDate;
	public double sellValue;

	@OneToOne
	public FinStock stock;
	public int quantity;

	@Override
	public String toString() {
		return "BacktestTransaction [buyDate=" + buyDate + ", buyValue=" + buyValue + ", sellDate=" + sellDate
				+ ", sellValue=" + sellValue + ", stock=" + stock.code + ", quantity=" + quantity + "]";
	}

}
