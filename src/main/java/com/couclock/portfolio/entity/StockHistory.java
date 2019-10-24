package com.couclock.portfolio.entity;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * SQL request to detect error <br/>
 * select count(*), date, stock_code from stock_history group by date,
 * stock_code having count(*) > 1<br/>
 *
 * @author dany
 *
 */
@Entity
@Table(indexes = { //
		@Index(name = "SH_DATE_IDX", columnList = "date"), //
		@Index(name = "SH_STOCK_IDX", columnList = "stock_code") //
})
public class StockHistory implements Comparable<StockHistory> {

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
	public int compareTo(StockHistory o) {
		return o.date.compareTo(this.date);
	}

	@Override
	public String toString() {
		return String.format("StockHistory [id=%s, stock=%s, date=%s, open=%s, close=%s, low=%s, high=%s, volume=%s]",
				id, stock.code, date, open, close, low, high, volume);
	}

}
