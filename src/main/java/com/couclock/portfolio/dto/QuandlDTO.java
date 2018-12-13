package com.couclock.portfolio.dto;

import java.util.List;

/**
 * Used to map that response :
 * https://www.quandl.com/api/v3/datasets/EURONEXT/UNA?column_index=1&api_key=fYGM2auetMskjNoqB2gx
 *
 * @author dany
 *
 */

public class QuandlDTO {

	public static class QuandlDataset {
		public String dataset_code;
		public String database_code;
		public List<List<String>> data;
	}

	public QuandlDataset dataset;

}
