package com.couclock.portfolio.service;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.couclock.portfolio.entity.FinStock;
import com.couclock.portfolio.entity.Portfolio;

@Service
public class SchedulingService {

	private static final Logger log = LoggerFactory.getLogger(SchedulingService.class);

	@Autowired
	private StockService stockService;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private StrategyService strategyService;
	@Autowired
	private YahooService yahooService;
	@Autowired
	private BoursoService boursoService;

	@Scheduled(fixedDelay = 1000 * 60 * 60)
	public void checkAndUpdatePortfolios() {

		List<FinStock> stocks = stockService.getAll();

		stocks.forEach(oneStock -> {
			try {
				yahooService.updateOneStockHistory(oneStock, true);
				boursoService.updateOneStockHistory(oneStock);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});

		List<Portfolio> portfolios = portfolioService.getAll();

		portfolios.forEach(onePortfolio -> {
			Portfolio updatedPortfolio;
			try {
				updatedPortfolio = strategyService.acceleratedDualMomentum(onePortfolio);
				updatedPortfolio = portfolioService.upsert(updatedPortfolio);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});

	}
}
