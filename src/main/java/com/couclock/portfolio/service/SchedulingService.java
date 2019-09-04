package com.couclock.portfolio.service;

import java.io.IOException;
import java.security.Provider;
import java.security.Security;
import java.util.List;

import javax.mail.Session;
import javax.mail.URLName;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.couclock.portfolio.entity.FinStock;
import com.couclock.portfolio.entity.Portfolio;
import com.couclock.portfolio.service.oauth2.OAuth2Utils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.mail.smtp.SMTPTransport;

@Service
public class SchedulingService {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class AccessTokenObject {
		@JsonProperty("access_token")
		private String accessToken;

		@JsonProperty("token_type")
		private String tokenType;

		@JsonProperty("expires_in")
		private int expiresIn;

		public String getAccessToken() {
			return accessToken;
		}

		public int getExpiresIn() {
			return expiresIn;
		}

		public String getTokenType() {
			return tokenType;
		}

		public void setAccessToken(String accessToken) {
			this.accessToken = accessToken;
		}

		public void setExpiresIn(int expiresIn) {
			this.expiresIn = expiresIn;
		}

		public void setTokenType(String tokenType) {
			this.tokenType = tokenType;
		}
	}

	public static final class OAuth2Provider extends Provider {
		private static final long serialVersionUIS = 1L;

		public OAuth2Provider() {
			super("Google OAuth2 Provider", 1.0, "Provides the XOAUTH2 SASL Mechanism");
			put("SaslClientFactory.XOAUTH2", "com.somedomain.oauth2.OAuth2SaslClientFactory");
		}
	}

	private static final Logger log = LoggerFactory.getLogger(SchedulingService.class);

	@Autowired
	private StockIndicatorService stockIndicatorService;
	@Autowired
	private StockService stockService;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private StrategyService strategyService;

	@Autowired
	private YahooService yahooService;

	@Autowired
	private OAuth2Utils oAuth2Utils;

	@Autowired
	private BoursoService boursoService;

	public static SMTPTransport connectToSmtp(Session session, String host, int port, String userEmail,
			String oauthToken, boolean debug) throws Exception {

		final URLName unusedUrlName = null;
		SMTPTransport transport = new SMTPTransport(session, unusedUrlName);
		// If the password is non-null, SMTP tries to do AUTH LOGIN.
		final String emptyPassword = "";
		transport.connect(host, port, userEmail, emptyPassword);

		return transport;
	}

	public static void initialize() {
		Security.addProvider(new OAuth2Provider());
	}

//	@Scheduled(fixedDelay = 1000 * 60 * 60 * 3, initialDelay = 1000)
	@Transactional
	public void checkAndUpdatePortfolios() {

		log.warn("**********************************");
		log.warn("Updating stocks and portfolios ...");
		log.warn("**********************************");

		ObjectMapper objectMapper = new ObjectMapper();

		List<FinStock> stocks = stockService.getAll();

		stocks.forEach(oneStock -> {
			try {
				yahooService.updateOneStockHistory(oneStock, true);
				boursoService.updateOneStockHistory(oneStock);
				stockIndicatorService.updateIndicators(oneStock, false);

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

				String body = "<pre>" + objectMapper.writeValueAsString(updatedPortfolio.currentStatus) + "</pre>";
				oAuth2Utils.sendEmail("dany.lecoq@gmail.com", "Portfolio status : " + updatedPortfolio.code, body);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});

	}

	/**
	 * Pour récupérer un access_token :<br/>
	 * - Google dev console
	 * (https://console.developers.google.com/apis/credentials?project=portfolio-232016)<br/>
	 * - Create credentials<br/>
	 * - Oauth client ID<br/>
	 * - Application type : other<br/>
	 * => client ID and client secret
	 *
	 * - Clone https://github.com/google/gmail-oauth2-tools.git<br/>
	 * - lancer l'outil python : ./oauth2.py --user=dany.lecoq@gmail.com
	 * --client_id=xxx --client_secret=xxx --generate_oauth2_token<br/>
	 * - suivre l'url et coller le code de vérification <br/>
	 * => access_token et refresh_token <br/>
	 * - Coller le refresh_token, client ID et client secret dans le properties
	 *
	 * @throws Exception
	 */
	public void sendEMail() throws Exception {
		checkAndUpdatePortfolios();

		oAuth2Utils.sendEmail("dany.lecoq@gmail.com", "Test subject", "Test body\nligne2<br/><b>Test HTML</b>");
	}

}
