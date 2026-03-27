package br.com.microservices.orchestrated.orderservice.core.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.microservices.orchestrated.orderservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.orderservice.core.document.Event;
import br.com.microservices.orchestrated.orderservice.core.dto.EventFilters;
import br.com.microservices.orchestrated.orderservice.core.repository.EventRepository;

@Service
public class EventService {
	
	@Autowired
	private EventRepository repository;
	
	private Logger logger = Logger.getLogger(Event.class.getName());
	
	
	public Event save(Event event) {
		return this.repository.save(event);
	}
	
	public void notifyEnding(Event event) {
		event.setOrderId(event.getOrderId());
		event.setCreatedAt(LocalDateTime.now());
		save(event);
		this.logger.info(
			"Order " + event.getOrderId() + " with saga notified! TransactionID: " 
			+ event.getTransactionId()
		);
	}
	
	public List<Event> findAll(){
		return this.repository.findAllByOrderByCreatedAtDesc();
	}
	
	public Event findByFilters(EventFilters filters) {
		validateEmptyFilters(filters);
		if(!filters.getOrderId().isEmpty()) {
			return findByOrderId(filters.getOrderId());
		}
		else {
			return findByTransactionId(filters.getTransactionalId());
		}
	}
	
	private void validateEmptyFilters(EventFilters filters) {
		if(filters.getOrderId().isEmpty() && filters.getTransactionalId().isEmpty()) {
			throw new ValidationException("OrderID or TransactionalID must be informed");
		}
	}
	
	private Event findByOrderId(String orderId) {
		return this.repository.findTop1ByOrderIdOrderByCreatedAtDesc(orderId)
			.orElseThrow(() -> new ValidationException("Evento not found by orderID"));
	}
	
	private Event findByTransactionId(String transactionId) {
		return this.repository.findTop1ByTransactionIdOrderByCreatedAtDesc(transactionId)
			.orElseThrow(() -> new ValidationException("Evento not found by orderID"));
	}
	
}
  