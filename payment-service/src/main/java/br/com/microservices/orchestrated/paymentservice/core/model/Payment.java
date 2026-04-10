package br.com.microservices.orchestrated.paymentservice.core.model;

import java.time.LocalDateTime;

import br.com.microservices.orchestrated.paymentservice.core.enums.EPaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "payment")
public class Payment {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(nullable = false)
	private String orderId;
	
	@Column(nullable = false)
	private String transactionId;
	
	@Column(nullable = false)
	private Integer totalItems;
	
	@Column(nullable = false)
	private Double totalAmount;
	
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private EPaymentStatus status;
	
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;
	
	@Column(nullable = false)
	private LocalDateTime updatedAt;
	
	
	public Payment() {
		
	}
	
	
	public Payment(Integer id, String orderId, String transactionId, Integer totalItems, Double totalAmount,
			EPaymentStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.id = id;
		this.orderId = orderId;
		this.transactionId = transactionId;
		this.totalItems = totalItems;
		this.totalAmount = totalAmount;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
	
	public Integer getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(Integer totalItems) {
		this.totalItems = totalItems;
	}

	public Double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public EPaymentStatus getStatus() {
		return status;
	}

	public void setStatus(EPaymentStatus status) {
		this.status = status;
	}


	@PrePersist
	public void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
		this.status = EPaymentStatus.PENDING;
	}
	
	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
	
}
