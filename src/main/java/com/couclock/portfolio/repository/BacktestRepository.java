package com.couclock.portfolio.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.couclock.portfolio.entity.Backtest;

public interface BacktestRepository extends JpaRepository<Backtest, Long> {

}
