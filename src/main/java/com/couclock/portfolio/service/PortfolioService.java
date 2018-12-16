package com.couclock.portfolio.service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.couclock.portfolio.entity.Portfolio;
import com.couclock.portfolio.entity.PortfolioHistory;
import com.couclock.portfolio.repository.PortfolioEventRepository;
import com.couclock.portfolio.repository.PortfolioHistoryRepository;
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

	@Autowired
	private PortfolioHistoryRepository portfolioHistoryRepository;

	public void deleteByStrategyCode(String strategyCode) {
		Portfolio pf = portfolioRepository.findByStrategyCodeIgnoreCase(strategyCode);
		if (pf != null) {
			portfolioRepository.delete(pf);
		}
	}

	public List<Portfolio> getAll() {
		return portfolioRepository.findAll();
	}

	public Portfolio getByStrategyCode(String strategyCode) {
		return portfolioRepository.findByStrategyCodeIgnoreCase(strategyCode);
	}

	public double getCAGR(final Portfolio portfolio) {

		List<PortfolioHistory> orderedPFHistory = portfolio.history.stream() //
				.sorted((h1, h2) -> h1.date.compareTo(h2.date))//
				.collect(Collectors.toList());

		double cagr = 0;

		if (orderedPFHistory.size() > 1) {
			PortfolioHistory first = orderedPFHistory.get(0);
			PortfolioHistory last = orderedPFHistory.get(orderedPFHistory.size() - 1);
			cagr = last.value / first.value;
			double years = ChronoUnit.DAYS.between(first.date, last.date) * 1.0;
			years = years / 365.0;
			years = 1 / years;

			cagr = Math.pow(cagr, years) - 1;
		}

		return cagr;
	}

	public double getUlcerIndex(final Portfolio portfolio) {

		double sumSq = 0;
		double maxValue = 0;
		for (PortfolioHistory oneHistory : portfolio.history) {
			if (oneHistory.value > maxValue) {
				maxValue = oneHistory.value;
			} else {
				sumSq = sumSq + Math.pow(100 * ((oneHistory.value / maxValue) - 1), 2);
			}
		}
		double ulcerIndex = Math.sqrt(sumSq / portfolio.history.size());
		return ulcerIndex;

	}

	/**
	 * Upsert a portfolio in database
	 *
	 * @param strategyCode
	 * @param portfolio
	 */
	public void upsert(Portfolio portfolio) {

		portfolio.cagr = getCAGR(portfolio);
		portfolio.ulcerIndex = getUlcerIndex(portfolio);

		portfolioRepository.save(portfolio);

	}

}
