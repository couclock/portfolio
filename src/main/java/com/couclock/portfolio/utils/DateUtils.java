package com.couclock.portfolio.utils;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

@Service
public class DateUtils {

	public LocalDate getLastDayOfMonth(LocalDate currentDate) {
		LocalDate lastDay = currentDate.plusMonths(1).withDayOfMonth(1).minusDays(1);
		return lastDay;
	}

}
