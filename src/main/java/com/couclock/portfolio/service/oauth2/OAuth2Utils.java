package com.couclock.portfolio.service.oauth2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.couclock.portfolio.service.SchedulingService.AccessTokenObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.mail.smtp.SMTPTransport;

@Service
public class OAuth2Utils {
	private static final Logger log = LoggerFactory.getLogger(OAuth2Utils.class);

	@Value("${secret.client_id}")
	private String clientId;
	@Value("${secret.client_secret}")
	private String clientSecret;
	@Value("${secret.refresh_token}")
	private String refreshToken;

	private String oAuthToken;

	public void sendEmail(String toEmail, String subject, String body) throws Exception {
		String email = "dany.lecoq@gmail.com";

		SMTPTransport smtpTransport = null;
		try {
			smtpTransport = OAuth2Authenticator.connectToSmtp("smtp.gmail.com", 587, email, oAuthToken, false);
		} catch (Exception e) {
			log.error("Error connecting to SMTP : refreshing Token");
			// Maybe it is because of expired oAuthToekn => refresh it
			oAuthToken = refreshToken();
		}
		if (smtpTransport == null) {
			// Second try
			smtpTransport = OAuth2Authenticator.connectToSmtp("smtp.gmail.com", 587, email, oAuthToken, false);
		}
		System.out.println("Successfully authenticated to SMTP.");

		MimeMessage message = new MimeMessage(OAuth2Authenticator.getSession(oAuthToken, false));
		message.setText(body, "UTF-8", "html");
		message.setSubject(subject);

		Address toAddress = new InternetAddress(toEmail);
		message.setRecipient(Message.RecipientType.TO, toAddress);

		smtpTransport.sendMessage(message, message.getAllRecipients());

		smtpTransport.close();
	}

	private String refreshToken() {

		HttpURLConnection conn = null;
		String accessToken = null;

		try {

			URL url = new URL("https://accounts.google.com/o/oauth2/token");

			Map<String, Object> params = new LinkedHashMap<>();
			params.put("client_id", clientId);
			params.put("client_secret", clientSecret);
			params.put("refresh_token", refreshToken);
			params.put("grant_type", "refresh_token");

			StringBuilder postData = new StringBuilder();
			for (Map.Entry<String, Object> param : params.entrySet()) {
				if (postData.length() != 0)
					postData.append('&');
				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}
			byte[] postDataBytes = postData.toString().getBytes("UTF-8");

			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
			conn.setRequestProperty("Content-language", "en-US");
			conn.setDoOutput(true);

			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.write(postDataBytes);
			wr.close();

			StringBuilder sb = new StringBuilder();
			Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			for (int c = in.read(); c != -1; c = in.read()) {
				sb.append((char) c);
			}

			String respString = sb.toString();

			// Read access token from json response
			ObjectMapper mapper = new ObjectMapper();
			AccessTokenObject accessTokenObj = mapper.readValue(respString, AccessTokenObject.class);
			accessToken = accessTokenObj.getAccessToken();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}

		return (accessToken);

	}

}
