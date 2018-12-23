package com.couclock.portfolio.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.NaturalId;

import com.couclock.portfolio.entity.sub.PortfolioAddMoneyEvent;
import com.couclock.portfolio.entity.sub.PortfolioBuyEvent;
import com.couclock.portfolio.entity.sub.PortfolioEvent;
import com.couclock.portfolio.entity.sub.PortfolioSellEvent;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(indexes = { @Index(name = "IDX_STRATCODE", columnList = "strategyCode") })
public class Portfolio implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 215161339820055103L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public long id;

	@NaturalId
	public String strategyCode;

	public double startMoney;
	public LocalDate startDate;
	public LocalDate endDate;
	@Embedded
	public PortfolioStatus endStatus;

	@ElementCollection
	public List<PortfolioStatistic> statistics = new ArrayList<>();

	@ElementCollection
	public List<PortfolioPeriod> periods = new ArrayList<>();

	public double cagr;
	public double ulcerIndex;

	public String usStockCode;
	public String exUsStockCode;
	public String bondStockCode;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "portfolio_code", referencedColumnName = "strategyCode")
	@JsonIgnore
	public List<PortfolioEvent> events = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
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
