package com.couclock.portfolio.entity.strategies;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import com.couclock.portfolio.entity.Portfolio;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class StrategyParameters {

	public static enum STRATEGY {
		ACCELERATED_MOMENTUM
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	public STRATEGY strategy;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "portfolio_code", referencedColumnName = "code")
	@JsonIgnore
	public Portfolio portfolio;

	public abstract List<String> getStockList();
}
