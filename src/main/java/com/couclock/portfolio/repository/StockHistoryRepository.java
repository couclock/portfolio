package com.couclock.portfolio.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.couclock.portfolio.entity.StockHistory;

public interface StockHistoryRepository extends JpaRepository<StockHistory, Long> {

	long deleteByStock_Code(String code);

	StockHistory findByStock_CodeAndDate(String code, LocalDate date);

	List<StockHistory> findByStock_CodeOrderByDateDesc(String code);

	StockHistory findTop1ByStock_CodeOrderByDateDesc(String code);

}
