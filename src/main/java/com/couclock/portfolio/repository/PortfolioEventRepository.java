package com.couclock.portfolio.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.couclock.portfolio.entity.sub.PortfolioEvent;

public interface PortfolioEventRepository extends JpaRepository<PortfolioEvent, Long> {

}
