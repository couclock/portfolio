package com.couclock.portfolio.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.couclock.portfolio.entity.StockIndicator;

public interface StockIndicatorRepository extends JpaRepository<StockIndicator, Long> {

	long deleteByStock_Code(String code);

	long deleteByStock_Id(long stockId);

	StockIndicator findByStock_CodeAndDate(String code, LocalDate date);

	List<StockIndicator> findByStock_CodeOrderByDateDesc(String code);

	StockIndicator findTop1ByStock_CodeOrderByDateDesc(String code);

	StockIndicator findTop1ByStock_IdOrderByDateDesc(long stockId);

}
