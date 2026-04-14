package br.com.microservices.orchestrated.inventoryservice.core.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.microservices.orchestrated.inventoryservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.inventoryservice.core.dto.Event;
import br.com.microservices.orchestrated.inventoryservice.core.dto.OrderProducts;
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
		}
		catch(Exception error) {
			this.logger.log(Level.SEVERE, "Error trying to update inventory: " + error);
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
	}
	
	private Inventory findInventoryByProductCode(String productCode) {
		return this.inventoryRepository.findByProductCode(productCode)
			.orElseThrow(() -> new ValidationException("Inventory not found by informed product"));
	}
	
}
