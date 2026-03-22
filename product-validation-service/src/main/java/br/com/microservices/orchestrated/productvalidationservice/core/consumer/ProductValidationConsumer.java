package br.com.microservices.orchestrated.productvalidationservice.core.consumer;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import br.com.microservices.orchestrated.productvalidationservice.core.utils.JsonUtil;

@Component
public class ProductValidationConsumer {
	
	private final Logger logger = Logger.getLogger(ProductValidationConsumer.class.getName());
	
	private JsonUtil jsonUtil;
	
	public ProductValidationConsumer(JsonUtil jsonUtil) {
		this.jsonUtil = jsonUtil;
	}
	
	
	@KafkaListener(
		groupId = "${spring.kafka.consumer.group-id}",
		topics = "${spring.kafka.topic.product-validation-success}"
	)
	public void consumeSuccessEventTopic(String payload) {
		this.logger.log(
			Level.INFO, "Receiving success event from product-validation-success topic " 
			+ payload
		);
		var event = this.jsonUtil.toEvent(payload);
		this.logger.log(Level.INFO, event.toString());
	}
	
	@KafkaListener(
		groupId = "${spring.kafka.consumer.group-id}",
		topics = "${spring.kafka.topic.product-validation-fail}"
	)
	public void consumeFailEventTopic(String payload) {
		this.logger.log(
			Level.INFO, "Receiving rollback event from product-validation-fail topic " 
			+ payload
		);
		var event = this.jsonUtil.toEvent(payload);
		this.logger.log(Level.INFO, event.toString());
	}
}
