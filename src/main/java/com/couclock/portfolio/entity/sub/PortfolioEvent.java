package com.couclock.portfolio.entity.sub;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class PortfolioEvent {

	public static enum EVENT_TYPE {
		SELL, BUY, ADD_MONEY
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	public LocalDate date = null;
	public EVENT_TYPE type = EVENT_TYPE.ADD_MONEY;

	public LocalDate getDate() {
		return date;
	}

}
