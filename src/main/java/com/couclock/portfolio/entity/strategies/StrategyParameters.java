package com.couclock.portfolio.entity.strategies;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;

import com.couclock.portfolio.entity.Portfolio;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class StrategyParameters {

	public static enum STRATEGY {
		ACCELERATED_MOMENTUM
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	public STRATEGY strategy;

	@OneToOne(fetch = FetchType.LAZY)
	@JsonIgnore
	public Portfolio portfolio;

	public StrategyParameters() {

	}

	public abstract List<String> getStockList();

}
