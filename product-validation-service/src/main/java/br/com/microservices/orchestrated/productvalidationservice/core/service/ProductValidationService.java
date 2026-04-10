package br.com.microservices.orchestrated.productvalidationservice.core.service;

import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.microservices.orchestrated.productvalidationservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.productvalidationservice.core.dto.Event;
import br.com.microservices.orchestrated.productvalidationservice.core.dto.History;
import br.com.microservices.orchestrated.productvalidationservice.core.dto.OrderProducts;
import br.com.microservices.orchestrated.productvalidationservice.core.enums.ESagaStatus;
import br.com.microservices.orchestrated.productvalidationservice.core.model.Validation;
import br.com.microservices.orchestrated.productvalidationservice.core.producer.KafkaProducer;
import br.com.microservices.orchestrated.productvalidationservice.core.repository.ProductRepository;
import br.com.microservices.orchestrated.productvalidationservice.core.repository.ValidationRepository;
import br.com.microservices.orchestrated.productvalidationservice.core.utils.JsonUtil;

@Service
public class ProductValidationService {
	
	private static final String CURRENT_SOURCE = "PRODUCT_VALIDATION_SERVICE";
	
	private Logger logger = Logger.getLogger(ProductValidationService.class.getName());
	
	@Autowired
	private JsonUtil jsonUtil;
	
	@Autowired
	private KafkaProducer producer;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private ValidationRepository validationRepository;
	
	
	public void validateExistingProducts(Event event) {
		try {
			checkCurrentValidation(event);
			createValidation(event, true);
			handleSuccess(event);
		}
		catch(Exception error) {
			this.logger.log(Level.SEVERE, "Error trying to validate products: " + error);
			handleFailCurrentNotExecuted(event, error.getMessage());
		}
		this.producer.sendEvent(this.jsonUtil.toJson(event));
	}
	
	private void validateProductsInformed(Event event) {
		
		if(event.getPayload() == null || event.getPayload().getProducts().isEmpty()) {
			throw new ValidationException("Product list is empty");
		}
		
		if(event.getPayload().getId().isEmpty() || event.getPayload().getTransactionId().isEmpty()) {
			throw new ValidationException("OrderId and TransactionId must be informed");
		}
	}
	
	private void checkCurrentValidation(Event event) {
		
		validateProductsInformed(event);
		
		if(this.validationRepository.existsByOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId())) {
			throw new ValidationException("There's another transactionId for this validation");
		}
		
		event.getPayload().getProducts().forEach(product -> {
			validateProductInformed(product);
			validateExistingProduct(product.getProduct().getCode());
		});

	}
	
	private void validateProductInformed(OrderProducts product) {
		if(product.getProduct() == null || product.getProduct().getCode().isEmpty()) {
			throw new ValidationException("Product must be informed!");
		}
	}
	
	private void validateExistingProduct(String code) {
		
		if(!this.productRepository.existsByCode(code)) {
			throw new ValidationException("Product does not exists in database");
		}
	}
	
	private void createValidation(Event event, Boolean success) {
		Validation validation = new Validation();
		validation.setOrderId(event.getPayload().getId());
		validation.setTransactionId(event.getTransactionId());
		validation.setSuccess(success);
		
		this.validationRepository.save(validation);
	}
	
	private void handleSuccess(Event event) {
		event.setStatus(ESagaStatus.SUCCESS);
		event.setSource(CURRENT_SOURCE);
		addHistory(event, "Products Are validated successfully!");
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
		addHistory(event, "Fail to validate products: ".concat(message));
	}
	
	public void rollbackEvent(Event event) {
		changeValidationToFailed(event);
		event.setStatus(ESagaStatus.FAIL);
		event.setSource(CURRENT_SOURCE);
		addHistory(event, "Rollback executed on product validation");
		producer.sendEvent(jsonUtil.toJson(event));
	}
	
	private void changeValidationToFailed(Event event) {
		this.validationRepository.findByOrderIdAndTransactionId(
			event.getPayload().getId(), event.getPayload().getTransactionId()
		)
		.ifPresentOrElse(validation -> {
			validation.setSuccess(false);
			this.validationRepository.save(validation);
		}, () -> createValidation(event, false));
	}
}
