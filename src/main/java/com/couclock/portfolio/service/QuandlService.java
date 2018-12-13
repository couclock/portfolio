package com.couclock.portfolio.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.couclock.portfolio.dto.QuandlDTO;
import com.couclock.portfolio.entity.FinStock;
import com.couclock.portfolio.entity.StockHistory;

@Service
public class QuandlService {

	private static final Logger log = LoggerFactory.getLogger(QuandlService.class);

	private final static String QUANDL_API_KEY = "fYGM2auetMskjNoqB2gx";

	@Autowired
	private StockService stockService;
	@Autowired
	private StockHistoryService stockHistoryService;

	public void updateEquityList() throws DataFormatException, IOException {

		// 1 : Get remote data
		RestTemplate restTemplate = new RestTemplate();
		byte[] compressedData = restTemplate.getForObject(
				"https://www.quandl.com/api/v3/databases/EURONEXT/metadata?api_key=" + QUANDL_API_KEY, byte[].class);

		// 2 : Unzip file
		byte[] buffer = new byte[1024];
		ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(compressedData));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		ZipEntry ze = zis.getNextEntry();

		while (ze != null) {
			String fileName = ze.getName();

			if (fileName.equals("EURONEXT_metadata.csv")) {
				int len;
				while ((len = zis.read(buffer)) > 0) {
					bos.write(buffer, 0, len);
				}
			}
			ze = zis.getNextEntry();
		}

		bos.close();
		byte[] decompressedData = bos.toByteArray();
		String decompressedString = new String(decompressedData);

		// 3 : Parse CSV content and upsert stock list
		CSVParser csvParser = new CSVParser(new StringReader(decompressedString),
				CSVFormat.DEFAULT.withHeader("code", "name", "description", "refreshed_at", "from_date", "to_date")
						.withIgnoreHeaderCase().withTrim().withFirstRecordAsHeader());

		FinStock finStock = new FinStock();
		Pattern currencyPattern = Pattern.compile("^.*Currency: (\\w*).*$");
		Pattern exchangePattern = Pattern.compile("^.*\\. Market: (.*)$");

		for (CSVRecord csvRecord : csvParser) {

			finStock = new FinStock();
			finStock.code = csvRecord.get("code");
			finStock.name = csvRecord.get("name");

			String description = csvRecord.get("description");

			finStock.description = description;

			Matcher matcher = currencyPattern.matcher(description);
			if (matcher.matches()) {
				finStock.currency = matcher.group(1);
			}

			matcher = exchangePattern.matcher(description);
			if (matcher.matches()) {
				finStock.stockExchange = matcher.group(1);
			}

			stockService.upsert(finStock);

		}

		csvParser.close();
	}

	public void updateOneStockHistory(String stockCode) {

		long start = System.currentTimeMillis();

		FinStock stock = stockService.getByCode(stockCode);

		// Skip unknown stock
		if (stock == null) {
			return;
		}

		StockHistory stockHistory = stockHistoryService.getLatestHistory(stockCode);

		String urlToCall = "https://www.quandl.com/api/v3/datasets/EURONEXT/" + stockCode + "?order=asc&api_key="
				+ QUANDL_API_KEY;

		if (stockHistory != null) {
			LocalDate startDate = stockHistory.date.plusDays(1);
			if (startDate.isBefore(LocalDate.now()) || startDate.isEqual(LocalDate.now())) {
				urlToCall += "&start_date=" + stockHistory.date.format(DateTimeFormatter.ISO_LOCAL_DATE) + "&end_date="
						+ LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
			} else {
				return;
			}
		}

		log.warn("Calling " + urlToCall + " (" + (System.currentTimeMillis() - start) + ")");
		start = System.currentTimeMillis();

		RestTemplate restTemplate = new RestTemplate();
		QuandlDTO data = restTemplate.getForObject(urlToCall, QuandlDTO.class);

		log.warn("Analysing data (" + (System.currentTimeMillis() - start) + ")");
		start = System.currentTimeMillis();

		List<StockHistory> toImport = new ArrayList<>();
		data.dataset.data.forEach(oneQuoteDay -> {
			try {
				StockHistory newStockHistory = new StockHistory();
				newStockHistory.stock = stock;
				newStockHistory.date = LocalDate.parse(oneQuoteDay.get(0), DateTimeFormatter.ISO_LOCAL_DATE);
				newStockHistory.open = Double.valueOf(oneQuoteDay.get(1));
				newStockHistory.high = Double.valueOf(oneQuoteDay.get(2));
				newStockHistory.low = Double.valueOf(oneQuoteDay.get(3));
				newStockHistory.close = Double.valueOf(oneQuoteDay.get(4));
				newStockHistory.volume = Double.valueOf(oneQuoteDay.get(5)).longValue();

				toImport.add(newStockHistory);
				// stockHistoryService.create(newStockHistory);
			} catch (NullPointerException npe) {
				log.error("Error parsing data : " + oneQuoteDay, npe);
			}

		});

		log.warn("Analized (" + (System.currentTimeMillis() - start) + ")");
		start = System.currentTimeMillis();

		stockHistoryService.createBatch(toImport);

		log.warn("Import ended (" + (System.currentTimeMillis() - start) + ")");

	}

	public void updateStocksHistory() {

		List<FinStock> stocks = stockService.getAll();
		int count = stocks.size();
		int current = 1;

		for (FinStock oneStock : stocks) {
			log.warn("[" + current++ + "/" + count + "] Updating " + oneStock.code);
			updateOneStockHistory(oneStock.code);
		}

	}

}
