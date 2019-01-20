package com.couclock.portfolio.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.couclock.portfolio.entity.strategies.StrategyParameters;
import com.couclock.portfolio.entity.sub.PortfolioAddMoneyEvent;
import com.couclock.portfolio.entity.sub.PortfolioBuyEvent;
import com.couclock.portfolio.entity.sub.PortfolioEvent;
import com.couclock.portfolio.entity.sub.PortfolioSellEvent;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(indexes = { @Index(name = "IDX_PFCODE", columnList = "code") })
public class Portfolio implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 215161339820055103L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public long id;

	public String code;

	public double startMoney;
	public double endMoney;

	public LocalDate startDate;
	public LocalDate endDate;

	@Embedded
	public PortfolioStatus currentStatus;

	@ElementCollection
	public List<PortfolioStatistic> statistics = new ArrayList<>();

	@ElementCollection
	public List<PortfolioPeriod> periods = new ArrayList<>();

	public double cagr;
	public double ulcerIndex;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	public StrategyParameters strategyParameters;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "portfolio_id")
	@JsonIgnore
	public List<PortfolioEvent> events = new ArrayList<>();

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "portfolio_id")
	@JsonIgnore
	public List<PortfolioHistory> history = new ArrayList<>();

	public void addAddMoneyEvent(LocalDate date, double amount) {
		events.add(new PortfolioAddMoneyEvent(date, amount));
	}

	public void addBuyEvent(LocalDate date, long count, String stockCode) {
		events.add(new PortfolioBuyEvent(date, count, stockCode));
	}

	public void addHistory(LocalDate date, double value) {
		history.add(new PortfolioHistory(date, value));
	}

	public void addSellEvent(LocalDate date, long count, String stockCode) {
		events.add(new PortfolioSellEvent(date, count, stockCode));
	}

}
