package com.couclock.portfolio.dto;

import java.util.List;

/**
 * Used to map that response :
 * https://www.boursorama.com/bourse/action/graph/ws/GetTicksEOD?symbol=1rTEPRE&length=180&period=0&guid=
 *
 * @author dany
 *
 */

public class BoursoDTO {

	public static class BoursoD {
		public String Name;
		public List<BoursoQuoteTab> QuoteTab;
	}

	public static class BoursoQuoteTab {

		public int d; // daycount from 1970-01-01
		public double o; // open
		public double c; // close
		public double h; // high
		public double l; // low
		public long v; // volume

	}

	public BoursoD d;

}
