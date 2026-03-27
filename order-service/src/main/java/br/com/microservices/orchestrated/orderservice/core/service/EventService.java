package br.com.microservices.orchestrated.orderservice.core.service;

import java.time.LocalDateTime;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.microservices.orchestrated.orderservice.core.document.Event;
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
	
}
