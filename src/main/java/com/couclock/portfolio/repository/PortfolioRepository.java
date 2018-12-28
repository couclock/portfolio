package com.couclock.portfolio.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.couclock.portfolio.entity.Portfolio;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

	Portfolio findByCodeIgnoreCase(String pfCode);

}
