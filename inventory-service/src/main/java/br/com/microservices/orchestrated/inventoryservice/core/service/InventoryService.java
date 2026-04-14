package br.com.microservices.orchestrated.inventoryservice.core.service;

import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.microservices.orchestrated.inventoryservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.inventoryservice.core.dto.Event;
import br.com.microservices.orchestrated.inventoryservice.core.dto.History;
import br.com.microservices.orchestrated.inventoryservice.core.dto.Order;
import br.com.microservices.orchestrated.inventoryservice.core.dto.OrderProducts;
import br.com.microservices.orchestrated.inventoryservice.core.enums.ESagaStatus;
import br.com.microservices.orchestrated.inventoryservice.core.model.Inventory;
import br.com.microservices.orchestrated.inventoryservice.core.model.OrderInventory;
import br.com.microservices.orchestrated.inventoryservice.core.producer.KafkaProducer;
import br.com.microservices.orchestrated.inventoryservice.core.repository.InventoryRepository;
import br.com.microservices.orchestrated.inventoryservice.core.repository.OrderInventoryRepository;
import br.com.microservices.orchestrated.inventoryservice.core.utils.JsonUtil;

@Service
public class InventoryService {
	
	private static final String CURRENT_SOURCE = "INVENTORY_SERVICE";
	
	private Logger logger = Logger.getLogger(InventoryService.class.getName());
	
	@Autowired
	private JsonUtil jsonUtil;
	
	@Autowired
	private KafkaProducer producer;
	
	@Autowired
	private InventoryRepository inventoryRepository;
	
	@Autowired
	private OrderInventoryRepository orderInventoryRepository;
	
	
	public void updateInventory(Event event) {
		
		try {
			checkCurrentValidation(event);
			createOrderInventory(event);
			updateInventory(event.getPayload());
			handleSuccess(event);
			
		}
		catch(Exception error) {
			this.logger.log(Level.SEVERE, "Error trying to update inventory: " + error);
			handleFailCurrentNotExecuted(event, error.getMessage());
		}
		this.producer.sendEvent(this.jsonUtil.toJson(event));
	}
	
	
	private void checkCurrentValidation(Event event) {
		if(this.orderInventoryRepository.existsByOrderIdAndTransactionId(event.getPayload().getId(), event.getPayload().getId())) {
			throw new ValidationException("There's another transactionId for this validation.");
		}
	}
	
	private void createOrderInventory(Event event) {
		event.getPayload()
			.getProducts()
			.forEach(product -> {
				Inventory inventory = findInventoryByProductCode(product.getProduct().getCode());
				OrderInventory orderInventory = createOrderInventory(event, product, inventory);
				this.orderInventoryRepository.save(orderInventory);
			});
	}
	
	private OrderInventory createOrderInventory(Event event, OrderProducts product, Inventory inventory) {
		OrderInventory orderInventory = new OrderInventory();
		orderInventory.setInventory(inventory);
		orderInventory.setOldQuantity(inventory.getAvailable());
		orderInventory.setOrderQuantity(product.getQuantity());
		orderInventory.setNewQuantity(inventory.getAvailable() - product.getQuantity());
		orderInventory.setOrderId(event.getPayload().getId());
		orderInventory.setTransactionId(event.getTransactionId());
		return orderInventory;
	}
	
	private void updateInventory(Order order) {
		order.getProducts()
			.forEach(product -> {
				Inventory inventory = findInventoryByProductCode(product.getProduct().getCode());
				checkInventory(inventory.getAvailable(), product.getQuantity());
				inventory.setAvailable(inventory.getAvailable() - product.getQuantity());
				this.inventoryRepository.save(inventory);
			});
	}
	
	private void checkInventory(Integer available, Integer orderQuantity) {
		if(orderQuantity > available) {
			throw new ValidationException("Product is out of stock");
		}
	}
	
	private Inventory findInventoryByProductCode(String productCode) {
		return this.inventoryRepository.findByProductCode(productCode)
			.orElseThrow(() -> new ValidationException("Inventory not found by informed product"));
	}
	
	private void handleSuccess(Event event) {
		event.setStatus(ESagaStatus.SUCCESS);
		event.setSource(CURRENT_SOURCE);
		addHistory(event, "Inventory updated successfully!");
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
		addHistory(event, "Fail to update inventory: ".concat(message));
	}
	
	public void rollbackInventory(Event event) {
		event.setStatus(ESagaStatus.FAIL);
		event.setSource(CURRENT_SOURCE);
		
		try {
			returnInventoryToPreviousValues(event);
			addHistory(event, "Rollback executed for inventory!");
		}
		catch(Exception error) {
			addHistory(event, "Rollback not executed for inventory: ".concat(error.getMessage()));
		}
		this.producer.sendEvent(jsonUtil.toJson(event));
	}
	
	private void returnInventoryToPreviousValues(Event event) {
		this.orderInventoryRepository
			.findByOrderIdAndTransactionId(event.getPayload().getId(), event.getTransactionId())
			.forEach(orderInventory -> {
				Inventory inventory = orderInventory.getInventory();
				inventory.setAvailable(orderInventory.getOldQuantity());
				this.inventoryRepository.save(inventory);	
			});
		
	}
	
}
