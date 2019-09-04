package com.couclock.portfolio.converter;

import java.io.IOException;
import java.util.Map;

import javax.persistence.AttributeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HashMapConverter implements AttributeConverter<Map<String, Object>, String> {

	private static final Logger logger = LoggerFactory.getLogger(HashMapConverter.class);
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String convertToDatabaseColumn(Map<String, Object> mapData) {

		String mapDataJson = null;
		try {
			mapDataJson = objectMapper.writeValueAsString(mapData);
		} catch (final JsonProcessingException e) {
			logger.error("JSON writing error", e);
		}

		return mapDataJson;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> convertToEntityAttribute(String mapDataJson) {

		Map<String, Object> mapData = null;
		try {
			mapData = objectMapper.readValue(mapDataJson, Map.class);
		} catch (final IOException e) {
			logger.error("JSON reading error", e);
		}

		return mapData;
	}
}
