package com.couclock.portfolio.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.couclock.portfolio.entity.Portfolio;
import com.couclock.portfolio.entity.PortfolioHistory;
import com.couclock.portfolio.entity.PortfolioStatistic;
import com.couclock.portfolio.entity.PortfolioStatus;
import com.couclock.portfolio.entity.StockHistory;
import com.couclock.portfolio.entity.sub.PortfolioBuyEvent;
import com.couclock.portfolio.entity.sub.PortfolioEvent;
import com.couclock.portfolio.entity.sub.PortfolioEvent.EVENT_TYPE;
import com.couclock.portfolio.entity.sub.PortfolioSellEvent;
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
	private StockHistoryService stockHistoryService;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private PortfolioEventRepository portfolioEventRepository;

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

	public List<PortfolioEvent> getEventsByStrategyCode(String strategyCode) {
		return portfolioEventRepository.findByPortfolio_StrategyCodeOrderByIdDesc(strategyCode);
	}

	public Map<String, PortfolioStatistic> getStatistics(Portfolio portfolio) throws Exception {

		List<PortfolioEvent> orderedEvents = portfolio.events.stream() //
				.sorted((e1, e2) -> {
					if (e1.date.isEqual(e2.date)) {
						return e1.type.compareTo(e2.type);
					} else {
						return e1.date.compareTo(e2.date);
					}
				}) //
				.collect(Collectors.toList());

		Map<String, Map<LocalDate, StockHistory>> stock2H = new HashMap<>();
		stock2H.put(portfolio.exUsStockCode, stockHistoryService.getAllByStockCode_Map(portfolio.exUsStockCode));
		stock2H.put(portfolio.usStockCode, stockHistoryService.getAllByStockCode_Map(portfolio.usStockCode));
		stock2H.put(portfolio.bondStockCode, stockHistoryService.getAllByStockCode_Map(portfolio.bondStockCode));

		Map<String, PortfolioStatistic> stock2stat = portfolio.statistics;

		stock2stat.put(portfolio.exUsStockCode, new PortfolioStatistic());
		stock2stat.put(portfolio.usStockCode, new PortfolioStatistic());
		stock2stat.put(portfolio.bondStockCode, new PortfolioStatistic());

		String currentStock = null;
		LocalDate currentStart = null;
		Double buyPrice = null;
		for (PortfolioEvent portfolioEvent : orderedEvents) {

			if (portfolioEvent.type.equals(EVENT_TYPE.BUY)) {
				PortfolioBuyEvent buyEvent = (PortfolioBuyEvent) portfolioEvent;
				currentStock = buyEvent.stockCode;
				currentStart = portfolioEvent.date;
				StockHistory sh = stockHistoryService.findFirstHistoryAfter(stock2H.get(currentStock), currentStart,
						currentStart.plusMonths(1));
				buyPrice = sh.open;

			} else if (portfolioEvent.type.equals(EVENT_TYPE.SELL)) {
				PortfolioSellEvent sellEvent = (PortfolioSellEvent) portfolioEvent;
				if (!sellEvent.stockCode.equals(currentStock)) {
					throw new Exception("Current SellEvent is not for right stockCode");
				}
				long curDuration = ChronoUnit.DAYS.between(currentStart, sellEvent.date);
				stock2stat.get(sellEvent.stockCode).dayCount += curDuration;
				StockHistory sh = stockHistoryService.findFirstHistoryAfter(stock2H.get(currentStock), sellEvent.date,
						sellEvent.date.plusMonths(1));
				double perf = (sh.open - buyPrice) / buyPrice;
				stock2stat.get(sellEvent.stockCode).performance += perf;
			}
		}

		return stock2stat;

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

	public Portfolio initPortfolio(Portfolio portfolio) {
		portfolio.startDate = LocalDate.parse("2010-01-01");
		portfolio.startMoney = 10000;
		portfolio.events.clear();
		portfolio.history.clear();

		portfolio.addAddMoneyEvent(portfolio.startDate, portfolio.startMoney);
		portfolio.endStatus = new PortfolioStatus();
		portfolio.endStatus.money = portfolio.startMoney;
		portfolio.endDate = portfolio.startDate;

		return portfolio;

	}

	/**
	 * Upsert a portfolio in database
	 *
	 * @param strategyCode
	 * @param portfolio
	 * @throws Exception
	 */
	public void upsert(Portfolio portfolio) throws Exception {

		portfolio.cagr = getCAGR(portfolio);
		portfolio.ulcerIndex = getUlcerIndex(portfolio);

		portfolio.statistics = getStatistics(portfolio);

		portfolioRepository.save(portfolio);

	}

}
