package br.com.microservices.orchestrated.orchestratorservice.core.dto;

import java.time.LocalDateTime;

import br.com.microservices.orchestrated.orchestratorservice.core.enums.EEventSource;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus;

public class History {
	
	private EEventSource source;
	
	private ESagaStatus status;
	
	private String message;
	
	private LocalDateTime createdAt;
	
	
	public History() {
		
	}
	
	public History(EEventSource source, ESagaStatus status, String message, LocalDateTime createdAt) {
		this.createdAt = createdAt;
		this.source = source;
		this.status = status;
		this.message = message;
	}


	public EEventSource getSource() {
		return source;
	}

	public void setSource(EEventSource source) {
		this.source = source;
	}

	public ESagaStatus getStatus() {
		return status;
	}

	public void setStatus(ESagaStatus status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
