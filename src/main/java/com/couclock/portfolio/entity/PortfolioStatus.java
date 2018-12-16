package com.couclock.portfolio.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;

@Embeddable
public class PortfolioStatus {

	@Embeddable
	public static class MyStock {

		public long count = 0;
		public String stockCode = null;

		public MyStock() {
		}

		public MyStock(long count, String stockCode) {
			this.count = count;
			this.stockCode = stockCode;
		}

		@Override
		public String toString() {
			return String.format("MyStock [count=%s, stockCode=%s]", count, stockCode);
		}

	}

	public double money = 0;

	@ElementCollection
	public List<MyStock> myStocks = new ArrayList<>();

	public void addStock(long count, String stockCode) {
		myStocks.add(new MyStock(count, stockCode));
	}

	public boolean containStock(String stockCode) {
		return myStocks.stream() //
				.anyMatch(oneStock -> {
					return oneStock.stockCode == stockCode;
				});
	}

	public void removeStock(String stockCode) {
		myStocks = myStocks.stream() //
				.filter(oneStock -> {
					return oneStock.stockCode != stockCode;
				}) //
				.collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return String.format("PortfolioStatus [money=%s, myStocks=%s]", money, myStocks);
	}

	@Override
	protected PortfolioStatus clone() {
		PortfolioStatus pfStatus = new PortfolioStatus();
		pfStatus.money = this.money;
		pfStatus.myStocks = new ArrayList<>(this.myStocks);
		return pfStatus;
	}

}
