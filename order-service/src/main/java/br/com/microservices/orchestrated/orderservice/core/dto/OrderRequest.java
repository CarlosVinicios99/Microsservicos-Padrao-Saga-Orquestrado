package br.com.microservices.orchestrated.orderservice.core.dto;

import java.util.List;

import br.com.microservices.orchestrated.orderservice.core.document.OrderProducts;

public class OrderRequest {
	
	private List<OrderProducts> products;
	
	
	public OrderRequest(List<OrderProducts> products) {
		this.products = products;
	}
	
	public OrderRequest() {
		
	}


	public List<OrderProducts> getProducts() {
		return products;
	}

	public void setProducts(List<OrderProducts> products) {
		this.products = products;
	}
}
