package br.com.microservices.orchestrated.paymentservice.core.consumer;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import br.com.microservices.orchestrated.paymentservice.core.service.PaymentService;
import br.com.microservices.orchestrated.paymentservice.core.utils.JsonUtil;

@Component
public class PaymentConsumer {
	
	private final Logger logger = Logger.getLogger(PaymentConsumer.class.getName());
	
	@Autowired
	private JsonUtil jsonUtil;
	
	@Autowired
	private PaymentService paymentService;
	
	@KafkaListener(
		groupId = "${spring.kafka.consumer.group-id}",
		topics = "${spring.kafka.topic.payment-success}"
	)
	public void consumeSuccessEventTopic(String payload) {
		this.logger.log(
			Level.INFO, "Receiving success event from payment-validation-success topic " 
			+ payload
		);
		var event = this.jsonUtil.toEvent(payload);
		this.logger.log(Level.INFO, event.toString());
		this.paymentService.realizePayment(event);
	}
	
	@KafkaListener(
		groupId = "${spring.kafka.consumer.group-id}",
		topics = "${spring.kafka.topic.payment-fail}"
	)
	public void consumeFailEventTopic(String payload) {
		this.logger.log(
			Level.INFO, "Receiving rollback event from payment-fail topic " 
			+ payload
		);
		var event = this.jsonUtil.toEvent(payload);
		this.logger.log(Level.INFO, event.toString());
		this.paymentService.realizeRefund(event);
	}
}
