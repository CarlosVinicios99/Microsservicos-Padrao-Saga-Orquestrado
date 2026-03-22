package br.com.microservices.orchestrated.inventoryservice.core.producer;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {
	
	private KafkaTemplate<String, String> kafkaTemplate;
	
	private Logger logger = Logger.getLogger(KafkaProducer.class.getName());
	
	@Value("${spring.kafka.topic.orchestrator}")
	private String orchestratorTopic;
	
	
	public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}
	
	
	public void sendEvent(String payload) {
		try {
			this.logger.log(
				Level.SEVERE, "Sending event to topic " + orchestratorTopic + 
				" with data " + payload
			);
			this.kafkaTemplate.send(orchestratorTopic, payload);
		}
		catch(Exception error) {
			this.logger.log(
				Level.SEVERE, "Error trying to send data to topic " + orchestratorTopic + 
				" " + "with data " + payload
			);
		}
	}
}
