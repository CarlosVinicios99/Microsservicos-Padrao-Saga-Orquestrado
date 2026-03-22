package br.com.microservices.orchestrated.orchestratorservice.core.utils;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.microservices.orchestrated.orchestratorservice.core.dto.Event;

@Component
public class JsonUtil {
	
	private final ObjectMapper objectMapper;
	
	public JsonUtil(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	
	
	public String toJson(Object object) {
		try {
			return this.objectMapper.writeValueAsString(object);
		}
		catch(Exception error) {
			return "";
		}
	}
	
	public Event toEvent(String json) {
		try {
			return this.objectMapper.readValue(json, Event.class);
		}
		catch(Exception error) {
			return null;
		}
	}
	
}
