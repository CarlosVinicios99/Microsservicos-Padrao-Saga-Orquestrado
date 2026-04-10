package br.com.microservices.orchestrated.paymentservice.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.microservices.orchestrated.paymentservice.core.model.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer>{
	
	

}
