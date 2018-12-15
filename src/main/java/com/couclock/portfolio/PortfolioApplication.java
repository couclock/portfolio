package com.couclock.portfolio;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.couclock.portfolio.entity.FinStock;
import com.couclock.portfolio.service.StockService;
import com.couclock.portfolio.service.YahooService;

@SpringBootApplication
public class PortfolioApplication {

	private static final Logger log = LoggerFactory.getLogger(PortfolioApplication.class);

	@Autowired
	private StockService stockService;

	@Autowired
	private YahooService yahooService;

	public static void main(String[] args) {

		// Se connecter sur yahoofinance via un brower et récupérer le B cookie
		System.setProperty("yahoofinance.cookie",
				"B=cb5aal9d49v8i&b=4&d=ZB44l3xrYH0t6op_1jk31WMfoze1aw--&s=37&i=8E7bPhwZxnYXtjYjPN3_");

		SpringApplication.run(PortfolioApplication.class, args);
	}

	@Bean
	public void insertETFs() throws IOException {

		FinStock stock = new FinStock();
		stock.code = "MMS";
		stockService.upsert(stock);

		stock = new FinStock();
		stock.code = "ESM";
		stockService.upsert(stock);

		stock = new FinStock();
		stock.code = "SMC";
		stockService.upsert(stock);

		stock = new FinStock();
		stock.code = "500";
		stockService.upsert(stock);

		stock = new FinStock();
		stock.code = "USTY";
		stockService.upsert(stock);

		// yahooService.updateStocksHistory();

	}

}
