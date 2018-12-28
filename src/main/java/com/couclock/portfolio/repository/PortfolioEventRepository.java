package com.couclock.portfolio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.couclock.portfolio.entity.sub.PortfolioEvent;

public interface PortfolioEventRepository extends JpaRepository<PortfolioEvent, Long> {

	List<PortfolioEvent> findByPortfolio_CodeOrderByIdDesc(String pfCode);

}
