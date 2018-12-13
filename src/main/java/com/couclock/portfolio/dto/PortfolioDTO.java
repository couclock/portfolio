package com.couclock.portfolio.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PortfolioDTO {

	public static class MyStock {
		public long count;
		public String stockCode;

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
		return String.format("PortfolioDTO [money=%s, myStocks=%s]", money, myStocks);
	}

}
