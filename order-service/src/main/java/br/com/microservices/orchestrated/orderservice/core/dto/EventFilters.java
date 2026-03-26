package br.com.microservices.orchestrated.orderservice.core.dto;

public class EventFilters {
	
	private String orderId;
	
	private String transactionalId;
	
	
	public EventFilters(String orderId, String transactionalId) {
		this.orderId = orderId;
		this.transactionalId = transactionalId;
	}
	
	public EventFilters() {
		
	}

	
	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getTransactionalId() {
		return transactionalId;
	}

	public void setTransactionalId(String transactionalId) {
		this.transactionalId = transactionalId;
	}
	
}
