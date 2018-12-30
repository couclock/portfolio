package com.couclock.portfolio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.couclock.portfolio.entity.FinStock;

public interface StockRepository extends JpaRepository<FinStock, Long> {

	List<FinStock> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
			String substringCode, String substringName, String substringDescription);

	FinStock findByCodeIgnoreCase(String code);

	List<FinStock> findByTagsIgnoreCaseOrderByCode(String tag);

}
