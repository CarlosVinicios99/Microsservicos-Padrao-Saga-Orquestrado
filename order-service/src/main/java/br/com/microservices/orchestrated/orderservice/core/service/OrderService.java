package br.com.microservices.orchestrated.orderservice.core.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.microservices.orchestrated.orderservice.core.document.Event;
import br.com.microservices.orchestrated.orderservice.core.document.Order;
import br.com.microservices.orchestrated.orderservice.core.dto.OrderRequest;
import br.com.microservices.orchestrated.orderservice.core.producer.SagaProducer;
import br.com.microservices.orchestrated.orderservice.core.repository.OrderRepository;
import br.com.microservices.orchestrated.orderservice.core.utils.JsonUtil;

@Service
public class OrderService {
		
	@Autowired
	private OrderRepository repository;
	
	@Autowired
	private JsonUtil jsonUtil;
	
	@Autowired 
	private SagaProducer producer;
	
	@Autowired
	private EventService eventService;
	
	
	public Order createOrder(OrderRequest orderRequest) {
		Order order = new Order();
		order.setProducts(orderRequest.getProducts());
		order.setCreatedAt(LocalDateTime.now());
		order.setTransactionId(String.format("%s_%s", Instant.now().toEpochMilli(), UUID.randomUUID()));
		
		this.repository.save(order);
		producer.sendEvent(this.jsonUtil.toJson(createPayload(order)));
		
		return order;
	}
	
	private Event createPayload(Order order) {
		Event event = new Event();
		event.setId(order.getId());
		event.setOrderId(order.getId());
		event.setTransactionId(order.getTransactionId());
		event.setPayload(order);
		event.setCreatedAt(LocalDateTime.now());
		event.setEventHistory(new ArrayList<>());
		
		eventService.save(event);
		return event;
	}
}
