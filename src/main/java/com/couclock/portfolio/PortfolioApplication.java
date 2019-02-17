package com.couclock.portfolio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PortfolioApplication {

	private static final Logger log = LoggerFactory.getLogger(PortfolioApplication.class);

	public static void main(String[] args) {

		// Se connecter sur yahoofinance via un brower et récupérer le B cookie
		System.setProperty("yahoofinance.cookie",
				"B=cb5aal9d49v8i&b=4&d=ZB44l3xrYH0t6op_1jk31WMfoze1aw--&s=37&i=8E7bPhwZxnYXtjYjPN3_");

		SpringApplication.run(PortfolioApplication.class, args);
	}

}
