package br.com.microservices.orchestrated.orderservice.core.consumer;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import br.com.microservices.orchestrated.orderservice.core.utils.JsonUtil;

@Component
public class EventConsumer {
	
	private final Logger logger = Logger.getLogger(EventConsumer.class.getName());
	
	private JsonUtil jsonUtil;
	
	public EventConsumer(JsonUtil jsonUtil) {
		this.jsonUtil = jsonUtil;
	}
	
	@KafkaListener(
		groupId = "${spring.kafka.consumer.group-id}",
		topics = "${spring.kafka.topic.notify-ending}"
	)
	public void consumeNotifyEndingTopic(String payload) {
		this.logger.log(
			Level.INFO, "Receiving ending notification event from notify-ending topic " 
			+ payload
		);
		var event = this.jsonUtil.toEvent(payload);
		this.logger.log(Level.INFO, event.toString());
	}
}
