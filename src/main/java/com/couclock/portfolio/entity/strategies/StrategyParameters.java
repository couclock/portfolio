package com.couclock.portfolio.entity.strategies;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
public abstract class StrategyParameters {

	public static enum STRATEGY {
		ACCELERATED_MOMENTUM
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	public StrategyParameters() {

	}

	public abstract boolean equals(StrategyParameters otherParameters);

}
