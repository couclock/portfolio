package com.couclock.portfolio.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.couclock.portfolio.dto.BoursoDTO;
import com.couclock.portfolio.entity.FinStock;
import com.couclock.portfolio.entity.StockHistory;

@Service
public class BoursoService {

	private static final Logger log = LoggerFactory.getLogger(BoursoService.class);

	@Autowired
	private StockHistoryService stockHistoryService;

	public void updateOneStockHistory(FinStock stock) {

		String urlToCall = "https://www.boursorama.com/bourse/action/graph/ws/GetTicksEOD?symbol=1rT" + stock.code
				+ "&length=7300&period=0&guid=";

		RestTemplate restTemplate = new RestTemplate();

		BoursoDTO histories = restTemplate.getForObject(urlToCall, BoursoDTO.class);

		Map<LocalDate, StockHistory> date2History = stockHistoryService.getAllByStockCode_Map(stock.code);

		List<StockHistory> toImport = new ArrayList<>();
		if (histories != null) {
			histories.d.QuoteTab.forEach(oneDay -> {

				LocalDate curDate = LocalDate.parse("1970-01-01").plusDays(oneDay.d);

				// Skip known history
				if (date2History != null && date2History.containsKey(curDate)) {
					log.debug("Skipping " + curDate + " (known) !");

					return;
				}

				log.info("toImport " + DateTimeFormatter.ISO_LOCAL_DATE.format(curDate) + " " + oneDay.o + " / "
						+ oneDay.c + " / " + oneDay.v);

				StockHistory newStockHistory = new StockHistory();
				newStockHistory.stock = stock;
				newStockHistory.date = curDate;
				newStockHistory.open = oneDay.o;
				newStockHistory.high = oneDay.h;
				newStockHistory.low = oneDay.l;
				newStockHistory.close = oneDay.c;
				newStockHistory.volume = oneDay.v;

				toImport.add(newStockHistory);

			});
		}

		log.warn("Stock[" + stock.code + "] History toImport count : " + toImport.size());

		stockHistoryService.createBatch(toImport);

	}

}
