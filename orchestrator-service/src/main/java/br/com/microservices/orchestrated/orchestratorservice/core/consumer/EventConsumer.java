package br.com.microservices.orchestrated.orchestratorservice.core.consumer;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import br.com.microservices.orchestrated.orchestratorservice.core.utils.JsonUtil;


@Component
public class EventConsumer {
	
	private final Logger logger = Logger.getLogger(EventConsumer.class.getName());
	
	private JsonUtil jsonUtil;
	
	public EventConsumer(JsonUtil jsonUtil) {
		this.jsonUtil = jsonUtil;
	}
	
	
	@KafkaListener(
		groupId = "${spring.kafka.consumer.group-id}",
		topics = "${spring.kafka.topic.start-saga}"
	)
	public void consumeStartSagaTopic(String payload) {
		this.logger.log(
			Level.INFO, "Receiving event from start-saga topic " 
			+ payload
		);
		var event = this.jsonUtil.toEvent(payload);
		this.logger.log(Level.INFO, event.toString());
	}
	
	@KafkaListener(
		groupId = "${spring.kafka.consumer.group-id}",
		topics = "${spring.kafka.topic.orchestrator}"
	)
	public void consumeOrchestratorTopic(String payload) {
		this.logger.log(
			Level.INFO, "Receiving event from orchestrator topic " 
			+ payload
		);
		var event = this.jsonUtil.toEvent(payload);
		this.logger.log(Level.INFO, event.toString());
	}
	
	@KafkaListener(
		groupId = "${spring.kafka.consumer.group-id}",
		topics = "${spring.kafka.topic.finish-success}"
	)
	public void consumeFinishSuccessTopic(String payload) {
		this.logger.log(
			Level.INFO, "Receiving event from finish-success topic " 
			+ payload
		);
		var event = this.jsonUtil.toEvent(payload);
		this.logger.log(Level.INFO, event.toString());
	}
	
	@KafkaListener(
		groupId = "${spring.kafka.consumer.group-id}",
		topics = "${spring.kafka.topic.finish-fail}"
	)
	public void consumeFinishFailTopic(String payload) {
		this.logger.log(
			Level.INFO, "Receiving event from finish-fail topic " 
			+ payload
		);
		var event = this.jsonUtil.toEvent(payload);
		this.logger.log(Level.INFO, event.toString());
	}
}
