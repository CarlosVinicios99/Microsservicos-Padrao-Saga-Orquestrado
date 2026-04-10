package br.com.microservices.orchestrated.paymentservice.core.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.microservices.orchestrated.paymentservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.paymentservice.core.dto.Event;
import br.com.microservices.orchestrated.paymentservice.core.model.Payment;
import br.com.microservices.orchestrated.paymentservice.core.producer.KafkaProducer;
import br.com.microservices.orchestrated.paymentservice.core.repository.PaymentRepository;
import br.com.microservices.orchestrated.paymentservice.core.utils.JsonUtil;

@Service
public class PaymentService {
	
	private static final String CURRENT_SOURCE = "PAYMENT_SERVICE";
	
	private Logger logger = Logger.getLogger(PaymentService.class.getName());
	
	@Autowired
	private JsonUtil jsonUtil;
	
	@Autowired
	private KafkaProducer producer;
	
	@Autowired
	private PaymentRepository paymentRepository;
	
	
	public void realizePayment(Event event) {
		try {
			checkCurrentValidation(event);
			createPendingPayment(event);
		}
		catch(Exception error) {
			this.logger.log(Level.SEVERE, "Error trying to make payment");
		}
		this.producer.sendEvent(this.jsonUtil.toJson(event));
	}
	
	private void checkCurrentValidation(Event event) {
		if(this.paymentRepository.existsByOrderIdAndTransactionId(event.getPayload().getId(), event.getPayload().getId())) {
			throw new ValidationException("There's another transactionId for this validation.");
		}
	}
	
	private void createPendingPayment(Event event) {
		
		Double totalAmount = calculateAmount(event);
		Integer totalItems = calculateTotalItems(event);
		
		Payment payment = new Payment();
		payment.setOrderId(event.getPayload().getId());
		payment.setTransactionId(event.getTransactionId());
		payment.setTotalAmount(totalAmount);
		payment.setTotalItems(totalItems);
		save(payment);
		setEventAmountItems(event, payment);
	}
	
	private Double calculateAmount(Event event) {
		return event.getPayload()
			.getProducts()
			.stream()
			.map(product -> product.getQuantity() * product.getProduct().getUnitValue())
			.reduce(0.0, Double::sum);
	}
	
	private Integer calculateTotalItems(Event event) {
		return event.getPayload()
			.getProducts()
			.stream()
			.map(product -> product.getQuantity())
			.reduce(0, Integer::sum);
	}
	
	private void setEventAmountItems(Event event, Payment payment) {
		event.getPayload().setTotalAmount(payment.getTotalAmount());
		event.getPayload().setTotalItems(payment.getTotalItems());
	}
	
	private void save(Payment payment) {
		this.paymentRepository.save(payment);
	}
	
}
