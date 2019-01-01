package com.couclock.portfolio;

import java.time.LocalDate;

public class Test {

	public static void main(String[] args) {

		LocalDate date1 = LocalDate.parse("2019-01-01");
		LocalDate date2 = LocalDate.parse("2019-01-01");

		System.out.println("date1 : " + date1);
		System.out.println("date2 : " + date2);

		System.out.println("date1.isBefore(date2) : " + date1.isBefore(date2));
		System.out.println("date1.isAfter(date2) : " + date1.isAfter(date2));
		System.out.println("date1.isEqual(date2) : " + date1.isEqual(date2));

		System.out.println("date2.isBefore(date1) : " + date2.isBefore(date1));
		System.out.println("date2.isAfter(date1) : " + date2.isAfter(date1));
		System.out.println("date2.isEqual(date1) : " + date2.isEqual(date1));

	}

}
