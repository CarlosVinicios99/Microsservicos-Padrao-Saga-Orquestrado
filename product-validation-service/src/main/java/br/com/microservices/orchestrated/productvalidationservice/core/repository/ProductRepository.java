package br.com.microservices.orchestrated.productvalidationservice.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.microservices.orchestrated.productvalidationservice.core.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>{
	
	Boolean existsByCode(String code);
	
}
