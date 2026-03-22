package br.com.microservices.orchestrated.orderservice.core.consumer;


import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SagaProducer {
		
	private KafkaTemplate<String, String> kafkaTemplate;
	
	private Logger logger = Logger.getLogger(SagaProducer.class.getName());
	
	@Value("${spring.kafka.topic.start-saga}")
	private String startSagaTopic;
	
	
	public SagaProducer(KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}
	
	
	public void sendEvent(String payload) {
		try {
			this.logger.log(
				Level.SEVERE, "Sending event to topic " + startSagaTopic + 
				" with data " + payload
			);
			this.kafkaTemplate.send(startSagaTopic, payload);
		}
		catch(Exception error) {
			this.logger.log(
				Level.SEVERE, "Error trying to send data to topic " + startSagaTopic + 
				" " + "with data " + payload
			);
		}
	}
}
