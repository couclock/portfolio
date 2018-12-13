package com.couclock.portfolio.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.NaturalId;

@Entity
public class FinStock implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 6421808740799425413L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public long id;

	@NaturalId
	public String code;

	public String name;
	public String description;
	public String currency;
	public String stockExchange;

}
