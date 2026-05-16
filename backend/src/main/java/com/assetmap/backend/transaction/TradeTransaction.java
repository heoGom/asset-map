package com.assetmap.backend.transaction;

import com.assetmap.backend.account.Account;
import com.assetmap.backend.common.entity.BaseEntity;
import com.assetmap.backend.securityitem.SecurityItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class TradeTransaction extends BaseEntity {

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

	@Column(nullable = false)
	private LocalDate tradeDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TradeType tradeType;

	@Column(nullable = false, precision = 19, scale = 6)
	private BigDecimal quantity;

	@Column(nullable = false, precision = 19, scale = 6)
	private BigDecimal price;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal grossAmount;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal fee;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal tax;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal netAmount;

	@Column(nullable = false)
	private String currency;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TransactionSource source;

	private String memo;

	protected TradeTransaction() {
	}

	public TradeTransaction(Long userId, Account account, SecurityItem securityItem, LocalDate tradeDate, TradeType tradeType, BigDecimal quantity, BigDecimal price, BigDecimal fee, BigDecimal tax, String currency, TransactionSource source, String memo) {
		this.userId = userId;
		this.account = account;
		this.securityItem = securityItem;
		this.tradeDate = tradeDate;
		this.tradeType = tradeType;
		this.quantity = quantity;
		this.price = price;
		this.fee = fee == null ? BigDecimal.ZERO : fee;
		this.tax = tax == null ? BigDecimal.ZERO : tax;
		this.currency = currency;
		this.source = source;
		this.memo = memo;
		recalculate();
	}

	public void update(TradeTransactionUpdateRequest request, Account account, SecurityItem securityItem) {
		if (account != null) this.account = account;
		if (securityItem != null) this.securityItem = securityItem;
		if (request.tradeDate() != null) this.tradeDate = request.tradeDate();
		if (request.tradeType() != null) this.tradeType = request.tradeType();
		if (request.quantity() != null) this.quantity = request.quantity();
		if (request.price() != null) this.price = request.price();
		if (request.fee() != null) this.fee = request.fee();
		if (request.tax() != null) this.tax = request.tax();
		if (request.currency() != null) this.currency = request.currency();
		if (request.source() != null) this.source = request.source();
		if (request.memo() != null) this.memo = request.memo();
		recalculate();
	}

	private void recalculate() {
		this.grossAmount = quantity.multiply(price);
		if (tradeType == TradeType.SELL) {
			this.netAmount = grossAmount.subtract(fee).subtract(tax);
			return;
		}
		this.netAmount = grossAmount.add(fee).add(tax);
	}

	public Long getId() { return id; }
	public Long getUserId() { return userId; }
	public Account getAccount() { return account; }
	public SecurityItem getSecurityItem() { return securityItem; }
	public LocalDate getTradeDate() { return tradeDate; }
	public TradeType getTradeType() { return tradeType; }
	public BigDecimal getQuantity() { return quantity; }
	public BigDecimal getPrice() { return price; }
	public BigDecimal getGrossAmount() { return grossAmount; }
	public BigDecimal getFee() { return fee; }
	public BigDecimal getTax() { return tax; }
	public BigDecimal getNetAmount() { return netAmount; }
	public String getCurrency() { return currency; }
	public TransactionSource getSource() { return source; }
	public String getMemo() { return memo; }
}
