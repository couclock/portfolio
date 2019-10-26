package com.couclock.portfolio.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OrderBy;

import com.couclock.portfolio.converter.StrategyParametersConverter;
import com.couclock.portfolio.entity.strategies.StrategyParameters;

@Entity
public class Backtest implements Serializable {

	private static final long serialVersionUID = 215161339820055103L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public long id;

	public String label;
	public double startMoney;
	public double currentMoney;
	public double estimatedValue;
	public double cagr;

	public LocalDate startDate;
	public LocalDate endDate;

	@ElementCollection(fetch = FetchType.EAGER)
	@OrderBy("buyDate")
	public List<BacktestTransaction> transactions = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(length = 25)
	public StrategyParameters.STRATEGY strategyCode;

	@Convert(converter = StrategyParametersConverter.class)
	public StrategyParameters strategyParameters;

}
