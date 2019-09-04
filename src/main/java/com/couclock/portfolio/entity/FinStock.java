package com.couclock.portfolio.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.NaturalId;

@Entity
@Table(indexes = { @Index(name = "CODE_IDX", columnList = "code") })
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

	@ElementCollection(fetch = FetchType.EAGER)
	public List<String> tags;

}
