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
import com.couclock.portfolio.entity.PortfolioPeriod;
import com.couclock.portfolio.entity.PortfolioStatistic;
import com.couclock.portfolio.entity.PortfolioStatus;
import com.couclock.portfolio.entity.StockHistory;
import com.couclock.portfolio.entity.strategies.AcceleratedMomentumStrategy;
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
	private StockService stockService;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private PortfolioEventRepository portfolioEventRepository;

	public void deleteByPortfolioCode(String pfCode) {
		Portfolio pf = portfolioRepository.findByCodeIgnoreCase(pfCode);
		if (pf != null) {
			portfolioRepository.delete(pf);
		}
	}

	/**
	 * Generate all possible portfolio for Accelerated Momentum strategy using
	 * existing stocks
	 *
	 * @throws Exception
	 */
	public void generatePortfolios() throws Exception {

		List<String> usStocks = stockService.findByTag("US").stream() //
				.map(oneStock -> oneStock.code) //
				.collect(Collectors.toList());

		List<String> exUsStocks = stockService.findByTag("ExUS").stream() //
				.map(oneStock -> oneStock.code) //
				.collect(Collectors.toList());

		List<String> bondStocks = stockService.findByTag("Bond").stream() //
				.map(oneStock -> oneStock.code) //
				.collect(Collectors.toList());

		for (String oneBondStock : bondStocks) {
			for (String oneExUsStock : exUsStocks) {
				for (String oneUsStock : usStocks) {
					String pfCode = oneUsStock + "_" + oneExUsStock + "_" + oneBondStock;

					Portfolio portfolio = getByPortfolioCode(pfCode);
					if (portfolio != null) {
						continue;
					} else {
						portfolio = new Portfolio();
					}
					portfolio.code = pfCode;

					this.initPortfolio(portfolio);

					AcceleratedMomentumStrategy strategyParameters = new AcceleratedMomentumStrategy();
					strategyParameters.addUsStock(1, oneUsStock);
					strategyParameters.addExUsStock(1, oneExUsStock);
					strategyParameters.addBondStock(1, oneBondStock);
					portfolio.strategyParameters = strategyParameters;

					this.upsert(portfolio);
				}
			}
		}

	}

	public List<Portfolio> getAll() {
		return portfolioRepository.findAll().stream() //
				.sorted((pf1, pf2) -> pf1.code.compareTo(pf2.code)) //
				.collect(Collectors.toList());

	}

	public Portfolio getByPortfolioCode(String pfCode) {
		return portfolioRepository.findByCodeIgnoreCase(pfCode);
	}

	public List<PortfolioEvent> getEventsByPortfolioCode(String pfCode) {
		return portfolioEventRepository.findByPortfolio_CodeOrderByIdDesc(pfCode);
	}

	public Portfolio initPortfolio(Portfolio portfolio) {
		portfolio.startDate = LocalDate.parse("2010-01-01");
		portfolio.startMoney = 10000;
		portfolio.events.clear();
		portfolio.history.clear();
		portfolio.periods.clear();
		portfolio.statistics.clear();

		portfolio.addAddMoneyEvent(portfolio.startDate, portfolio.startMoney);
		portfolio.currentStatus = new PortfolioStatus();
		portfolio.currentStatus.money = portfolio.startMoney;
		portfolio.endDate = portfolio.startDate;
		portfolio.endMoney = portfolio.startMoney;

		return portfolio;

	}

	/**
	 * Upsert a portfolio in database
	 *
	 * @param code
	 * @param portfolio
	 * @throws Exception
	 */
	public void upsert(Portfolio portfolio) throws Exception {

		portfolio.cagr = getCAGR(portfolio);
		portfolio.ulcerIndex = getUlcerIndex(portfolio);

		processStatistics(portfolio);

		portfolioRepository.save(portfolio);

	}

	private double getCAGR(final Portfolio portfolio) {

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

	private double getUlcerIndex(final Portfolio portfolio) {

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

	private void processStatistics(Portfolio portfolio) throws Exception {

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
		for (String oneStock : portfolio.strategyParameters.getStockList()) {
			stock2H.put(oneStock, stockHistoryService.getAllByStockCode_Map(oneStock));
		}

		portfolio.periods.clear();
		portfolio.statistics.clear();

		Map<String, PortfolioPeriod> stock2period = new HashMap<>();
		Map<String, PortfolioStatistic> stock2stat = new HashMap<>();

		for (PortfolioEvent portfolioEvent : orderedEvents) {

			if (portfolioEvent.type.equals(EVENT_TYPE.BUY)) {
				PortfolioBuyEvent buyEvent = (PortfolioBuyEvent) portfolioEvent;
				PortfolioPeriod pfP = new PortfolioPeriod();
				pfP.stockCode = buyEvent.stockCode;
				pfP.startDate = portfolioEvent.date;
				pfP.buyPrice = stock2H.get(buyEvent.stockCode).get(portfolioEvent.date).open;
				pfP.count = buyEvent.count;
				stock2period.put(buyEvent.stockCode, pfP);

			} else if (portfolioEvent.type.equals(EVENT_TYPE.SELL)) {
				PortfolioSellEvent sellEvent = (PortfolioSellEvent) portfolioEvent;
				if (!stock2period.containsKey(sellEvent.stockCode)) {
					throw new Exception("Current SellEvent (" + sellEvent + ") cannot be linked to buyEvent");
				}
				PortfolioPeriod pfP = stock2period.get(sellEvent.stockCode);
				pfP.endDate = sellEvent.date;
				pfP.sellPrice = stock2H.get(sellEvent.stockCode).get(portfolioEvent.date).open;
				pfP.processDuration();
				pfP.processPerf();

				portfolio.periods.add(pfP);
				stock2period.remove(sellEvent.stockCode);

				if (!stock2stat.containsKey(sellEvent.stockCode)) {
					stock2stat.put(sellEvent.stockCode, new PortfolioStatistic());
					stock2stat.get(sellEvent.stockCode).stockCode = sellEvent.stockCode;
				}
				stock2stat.get(sellEvent.stockCode).dayCount += pfP.duration;
				stock2stat.get(sellEvent.stockCode).performance += pfP.perf;
			}
		}

		portfolio.statistics.addAll(stock2stat.values());

	}

}
