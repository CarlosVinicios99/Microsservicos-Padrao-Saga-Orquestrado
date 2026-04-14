package br.com.microservices.orchestrated.paymentservice.core.service;

import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.microservices.orchestrated.paymentservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.paymentservice.core.dto.Event;
import br.com.microservices.orchestrated.paymentservice.core.dto.History;
import br.com.microservices.orchestrated.paymentservice.core.enums.EPaymentStatus;
import br.com.microservices.orchestrated.paymentservice.core.enums.ESagaStatus;
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
			Payment payment = findByOrderIdAndTransactionId(event);
			validateAmount(payment.getTotalAmount());
			changePaymentToSuccess(payment);
			handleSuccess(event);
		}
		catch(Exception error) {
			this.logger.log(Level.SEVERE, "Error trying to make payment: " + error);
			handleFailCurrentNotExecuted(event, error.getMessage());
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
	
	private void validateAmount(Double amount) {
		if(amount < 0.01) {
			throw new ValidationException("The minimum amount available is 0.01");
		}
	}
	
	private void changePaymentToSuccess(Payment payment) {
		payment.setStatus(EPaymentStatus.SUCCESS);
		save(payment);
	}
	
	private void handleSuccess(Event event) {
		event.setStatus(ESagaStatus.SUCCESS);
		event.setSource(CURRENT_SOURCE);
		addHistory(event, "Payment realized successfully!");
	}
	
	private void addHistory(Event event, String message) {
		History history = new History();
		
		history.setSource(event.getSource());
		history.setStatus(event.getStatus());
		history.setMessage(message);
		history.setCreatedAt(LocalDateTime.now());
		
		event.addToHistory(history);
	}
	
	private void handleFailCurrentNotExecuted(Event event, String message) {
		event.setStatus(ESagaStatus.ROLLBACK_PENDING);
		event.setSource(CURRENT_SOURCE);
		addHistory(event, "Fail to realize payment: ".concat(message));
	}
	
	public void realizeRefund(Event event) {
		
		event.setStatus(ESagaStatus.FAIL);
		event.setSource(CURRENT_SOURCE);
		
		try {
			changePaymentStatusToRefund(event);
			addHistory(event, "Rollback executed for payment!");
		}
		catch(Exception error) {
			addHistory(event, "Rollback not executed for payment!");
		}	
		this.producer.sendEvent(jsonUtil.toJson(event));
	}
	
	private void changePaymentStatusToRefund(Event event) {
		Payment payment = findByOrderIdAndTransactionId(event);
		payment.setStatus(EPaymentStatus.REFUND);
		setEventAmountItems(event, payment);
		save(payment);
	}
	
	private Payment findByOrderIdAndTransactionId(Event event) {
		return this.paymentRepository
			.findByOrderIdAndTransactionId(event.getPayload().getId(), event.getTransactionId())
			.orElseThrow(() -> new ValidationException("Payment not found by orderId and TransactionId"));
	}
	
	private void save(Payment payment) {
		this.paymentRepository.save(payment);
	}
	
}
