package br.com.microservices.orchestrated.inventoryservice.core.consumer;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import br.com.microservices.orchestrated.inventoryservice.core.service.InventoryService;
import br.com.microservices.orchestrated.inventoryservice.core.utils.JsonUtil;

@Component
public class InventoryConsumer {
	
	private final Logger logger = Logger.getLogger(InventoryConsumer.class.getName());
	
	@Autowired
	private JsonUtil jsonUtil;
	
	@Autowired
	private InventoryService inventoryService;
	
	@KafkaListener(
		groupId = "${spring.kafka.consumer.group-id}",
		topics = "${spring.kafka.topic.inventory-success}"
	)
	public void consumeSuccessEventTopic(String payload) {
		this.logger.log(
			Level.INFO, "Receiving success event from inventory-success topic " 
			+ payload
		);
		var event = this.jsonUtil.toEvent(payload);
		this.inventoryService.updateInventory(event);
	}
	
	@KafkaListener(
		groupId = "${spring.kafka.consumer.group-id}",
		topics = "${spring.kafka.topic.inventory-fail}"
	)
	public void consumeFailEventTopic(String payload) {
		this.logger.log(
			Level.INFO, "Receiving rollback event from inventory-fail topic " 
			+ payload
		);
		var event = this.jsonUtil.toEvent(payload);
		this.logger.log(Level.INFO, event.toString());
		this.inventoryService.rollbackInventory(event);
	}
}
