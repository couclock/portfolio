package com.couclock.portfolio.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;

@Embeddable
public class PortfolioStatus {

	@Embeddable
	public static class MyCurrentStock {

		public long count = 0;
		public String stockCode = null;

		public MyCurrentStock() {
		}

		public MyCurrentStock(long count, String stockCode) {
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
	public List<MyCurrentStock> currentStocks = new ArrayList<>();

	@ElementCollection
	public List<StockDistribution> toBuy = new ArrayList<>();

	@ElementCollection
	public List<String> toSell = new ArrayList<>();

	public void addStock(long count, String stockCode) {
		Optional<MyCurrentStock> found = currentStocks.stream() //
				.filter(oneStock -> {
					return oneStock.stockCode == stockCode;
				}) //
				.findFirst();
		if (found.isPresent()) {
			found.get().count = found.get().count + count;
		} else {
			currentStocks.add(new MyCurrentStock(count, stockCode));
		}
	}

	public boolean containStock(String stockCode) {
		return currentStocks.stream() //
				.anyMatch(oneStock -> {
					return oneStock.stockCode == stockCode;
				});
	}

	public MyCurrentStock getStock(String stockCode) {
		Optional<MyCurrentStock> found = currentStocks.stream() //
				.filter(oneStock -> {
					return oneStock.stockCode.equals(stockCode);
				}) //
				.findFirst();

		if (found.isPresent()) {
			return found.get();
		} else {
			return null;
		}
	}

	public void removeStock(String stockCode) {
		currentStocks = currentStocks.stream() //
				.filter(oneStock -> {
					return oneStock.stockCode != stockCode;
				}) //
				.collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return String.format("PortfolioStatus [money=%s, myStocks=%s, toBuy=%s, toSell=%s]", money, currentStocks,
				toBuy, toSell);
	}

}
