package br.com.microservices.orchestrated.productvalidationservice.core.dto;

import java.time.LocalDateTime;
import java.util.List;
import br.com.microservices.orchestrated.productvalidationservice.core.enums.ESagaStatus;

public class Event {
	
	private String id;
	
	private String transactionId;
	
	private String orderId;
	
	private Order payload;
	
	private String source;
	
	private ESagaStatus status;
	
	private List<History> eventHistory;
	
	private LocalDateTime createdAt;
	
	
	public Event() {
		
	}
	
	public Event(
		String id, String transactionId, String orderId, Order payload, String source, 
		ESagaStatus status,List<History> eventHistory, LocalDateTime createdAt
	) {
		this.id = id;
		this.transactionId = transactionId;
		this.orderId = orderId;
		this.payload = payload;
		this.source = source;
		this.status = status;
		this.eventHistory = eventHistory;
		this.createdAt = createdAt;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public Order getPayload() {
		return payload;
	}

	public void setPayload(Order payload) {
		this.payload = payload;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public ESagaStatus getStatus() {
		return status;
	}

	public void setStatus(ESagaStatus status) {
		this.status = status;
	}

	public List<History> getEventHistory() {
		return eventHistory;
	}

	public void setEventHistory(List<History> eventHistory) {
		this.eventHistory = eventHistory;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
