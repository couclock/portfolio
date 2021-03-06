package com.couclock.portfolio.entity;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class PortfolioHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	public LocalDate date;

	public double value;

	public PortfolioHistory() {
	}

	public PortfolioHistory(LocalDate date, double value) {
		this.date = date;
		this.value = value;
	}

	@Override
	public String toString() {
		return String.format("PortfolioHistory [id=%s, date=%s, value=%s]", id, date, value);
	}

}
