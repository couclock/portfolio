package com.couclock.portfolio.dto;

import java.time.LocalDate;
import java.util.List;

import com.couclock.portfolio.entity.FinStock;
import com.couclock.portfolio.entity.StockHistory;
import com.couclock.portfolio.entity.StockIndicator;

/**
 * Used to display stock list
 *
 * @author dany
 *
 */

public class LightStockDTO {

	public long id;

	public String code;

	public String name;

	public List<String> tags;

	public LocalDate lastHistoryDate;
	public LocalDate lastIndicatorDate;

	public LightStockDTO(FinStock stock, StockHistory lastHistory, StockIndicator lastIndicator) {
		this.id = stock.id;
		this.code = stock.code;
		this.name = stock.name;
		this.tags = stock.tags;
		this.lastHistoryDate = lastHistory != null ? lastHistory.date : null;
		this.lastIndicatorDate = lastIndicator != null ? lastIndicator.date : null;

	}

}
