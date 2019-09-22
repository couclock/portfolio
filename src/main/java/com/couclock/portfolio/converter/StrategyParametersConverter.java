package com.couclock.portfolio.converter;

import java.io.IOException;

import javax.persistence.AttributeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couclock.portfolio.entity.strategies.StrategyParameters;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StrategyParametersConverter implements AttributeConverter<StrategyParameters, String> {

	private static final Logger logger = LoggerFactory.getLogger(StrategyParametersConverter.class);
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String convertToDatabaseColumn(StrategyParameters stratParamData) {

		String stratParamDataJson = null;
		try {
			stratParamDataJson = objectMapper.writeValueAsString(stratParamData);
		} catch (final JsonProcessingException e) {
			logger.error("JSON writing error", e);
		}

		return stratParamDataJson;
	}

	@SuppressWarnings("unchecked")
	@Override
	public StrategyParameters convertToEntityAttribute(String stratParamDataJson) {

		StrategyParameters stratParamData = null;
		try {
			stratParamData = objectMapper.readValue(stratParamDataJson, StrategyParameters.class);
		} catch (final IOException e) {
			logger.error("JSON reading error", e);
		}

		return stratParamData;
	}
}
