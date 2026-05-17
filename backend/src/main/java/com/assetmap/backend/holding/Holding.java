package com.assetmap.backend.holding;

import com.assetmap.backend.account.Account;
import com.assetmap.backend.common.entity.BaseEntity;
import com.assetmap.backend.securityitem.SecurityItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;

@Entity
public class Holding extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long userId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "security_item_id", nullable = false)
	private SecurityItem securityItem;

	@Column(nullable = false, precision = 19, scale = 6)
	private BigDecimal quantity;

	@Column(nullable = false, precision = 19, scale = 6)
	private BigDecimal averagePrice;

	@Column(nullable = false, precision = 19, scale = 6)
	private BigDecimal currentPrice;

	@Column(nullable = false)
	private String currency;

	protected Holding() {
	}

	public Holding(
			Long userId,
			Account account,
			SecurityItem securityItem,
			BigDecimal quantity,
			BigDecimal averagePrice,
			BigDecimal currentPrice,
			String currency
	) {
		this.userId = userId;
		this.account = account;
		this.securityItem = securityItem;
		this.quantity = quantity;
		this.averagePrice = averagePrice;
		this.currentPrice = currentPrice;
		this.currency = currency;
	}

	public void update(HoldingUpdateRequest request, Account account, SecurityItem securityItem) {
		if (request.userId() != null) this.userId = request.userId();
		if (account != null) this.account = account;
		if (securityItem != null) this.securityItem = securityItem;
		if (request.quantity() != null) this.quantity = request.quantity();
		if (request.averagePrice() != null) this.averagePrice = request.averagePrice();
		if (request.currentPrice() != null) this.currentPrice = request.currentPrice();
		if (request.currency() != null) this.currency = request.currency();
	}

	public void replacePosition(BigDecimal quantity, BigDecimal averagePrice, BigDecimal currentPrice, String currency) {
		this.quantity = quantity;
		this.averagePrice = averagePrice;
		this.currentPrice = currentPrice;
		this.currency = currency;
	}

	public void updateCurrentPrice(BigDecimal currentPrice) {
		this.currentPrice = currentPrice;
	}

	public Long getId() {
		return id;
	}

	public Long getUserId() {
		return userId;
	}

	public Account getAccount() {
		return account;
	}

	public SecurityItem getSecurityItem() {
		return securityItem;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public BigDecimal getAveragePrice() {
		return averagePrice;
	}

	public BigDecimal getCurrentPrice() {
		return currentPrice;
	}

	public String getCurrency() {
		return currency;
	}
}
