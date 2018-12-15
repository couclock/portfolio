package com.couclock.portfolio.service;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.couclock.portfolio.entity.Portfolio;
import com.couclock.portfolio.repository.PortfolioEventRepository;
import com.couclock.portfolio.repository.PortfolioRepository;

/**
 *
 * @author dany
 *
 */
@Service
public class PortfolioService {

	private static final Logger log = LoggerFactory.getLogger(PortfolioService.class);

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private PortfolioEventRepository portfolioEventRepository;

	public Portfolio getByStrategyCode(String strategyCode) {
		return portfolioRepository.findByStrategyCodeIgnoreCase(strategyCode);
	}

	public void upsert(String strategyCode, Portfolio newPortfolio) {

		Portfolio portfolio = portfolioRepository.findByStrategyCodeIgnoreCase(strategyCode);
		if (portfolio == null) {
			portfolio = new Portfolio();
			portfolio.strategyCode = strategyCode;
		}
		portfolio.startDate = newPortfolio.startDate;
		portfolio.startMoney = newPortfolio.startMoney;
		portfolio.endDate = newPortfolio.endDate;
		portfolio.endMoney = newPortfolio.endMoney;

		// portfolio.events.forEach(oneEvent -> {
//			portfolioEventRepository.delete(oneEvent);
//		});
		portfolio.events.clear();
		portfolio.events.addAll(newPortfolio.events //
				.stream() //
				.map(oneEvent -> portfolioEventRepository.save(oneEvent)) //
				.collect(Collectors.toList()));

		portfolioRepository.save(portfolio);

	}

}
