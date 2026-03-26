package br.com.microservices.orchestrated.orderservice.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.microservices.orchestrated.orderservice.core.document.Event;
import br.com.microservices.orchestrated.orderservice.core.repository.EventRepository;

@Service
public class EventService {
	
	@Autowired
	private EventRepository repository;
	
	
	public Event save(Event event) {
		return this.repository.save(event);
	}
	
}
