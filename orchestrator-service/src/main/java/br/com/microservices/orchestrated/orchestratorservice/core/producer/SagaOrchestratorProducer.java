package br.com.microservices.orchestrated.orchestratorservice.core.producer;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SagaOrchestratorProducer {
	
	private KafkaTemplate<String, String> kafkaTemplate;
	
	private Logger logger = Logger.getLogger(SagaOrchestratorProducer.class.getName());

	
	public SagaOrchestratorProducer(KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}
	
	
	public void sendEvent(String payload, String topic) {
		try {
			this.logger.log(
				Level.SEVERE, "Sending event to topic " + topic + 
				" with data " + payload
			);
			this.kafkaTemplate.send(topic, payload);
		}
		catch(Exception error) {
			this.logger.log(
				Level.SEVERE, "Error trying to send data to topic " + topic + 
				" " + "with data " + payload
			);
		}
	}
}
