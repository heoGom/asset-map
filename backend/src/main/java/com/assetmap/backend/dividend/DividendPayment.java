package com.assetmap.backend.dividend;

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
public class DividendPayment extends BaseEntity {

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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dividend_event_id")
	private DividendEvent dividendEvent;

	@Column(nullable = false, precision = 19, scale = 6)
	private BigDecimal quantityAtRecordDate;

	@Column(nullable = false, precision = 19, scale = 6)
	private BigDecimal dividendPerShare;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal grossAmount;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal taxAmount;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal netAmount;

	@Column(nullable = false)
	private String currency;

	@Column(nullable = false, precision = 19, scale = 6)
	private BigDecimal exchangeRate;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal grossAmountKrw;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal netAmountKrw;

	private LocalDate paymentDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DividendPaymentStatus status;

	protected DividendPayment() {
	}

	public DividendPayment(Long userId, Account account, SecurityItem securityItem, DividendEvent dividendEvent, BigDecimal quantityAtRecordDate, BigDecimal dividendPerShare, BigDecimal taxAmount, String currency, BigDecimal exchangeRate, LocalDate paymentDate, DividendPaymentStatus status) {
		this.userId = userId;
		this.account = account;
		this.securityItem = securityItem;
		this.dividendEvent = dividendEvent;
		this.quantityAtRecordDate = quantityAtRecordDate;
		this.dividendPerShare = dividendPerShare;
		this.taxAmount = taxAmount == null ? BigDecimal.ZERO : taxAmount;
		this.currency = currency;
		this.exchangeRate = exchangeRate == null ? BigDecimal.ONE : exchangeRate;
		this.paymentDate = paymentDate;
		this.status = status;
		recalculate();
	}

	public void update(DividendPaymentUpdateRequest request, Account account, SecurityItem securityItem, DividendEvent dividendEvent) {
		if (request.userId() != null) this.userId = request.userId();
		if (account != null) this.account = account;
		if (securityItem != null) this.securityItem = securityItem;
		if (dividendEvent != null) this.dividendEvent = dividendEvent;
		if (request.quantityAtRecordDate() != null) this.quantityAtRecordDate = request.quantityAtRecordDate();
		if (request.dividendPerShare() != null) this.dividendPerShare = request.dividendPerShare();
		if (request.taxAmount() != null) this.taxAmount = request.taxAmount();
		if (request.currency() != null) this.currency = request.currency();
		if (request.exchangeRate() != null) this.exchangeRate = request.exchangeRate();
		if (request.paymentDate() != null) this.paymentDate = request.paymentDate();
		if (request.status() != null) this.status = request.status();
		recalculate();
	}

	private void recalculate() {
		this.grossAmount = quantityAtRecordDate.multiply(dividendPerShare);
		this.netAmount = grossAmount.subtract(taxAmount);
		this.grossAmountKrw = grossAmount.multiply(exchangeRate);
		this.netAmountKrw = netAmount.multiply(exchangeRate);
	}

	public Long getId() { return id; }
	public Long getUserId() { return userId; }
	public Account getAccount() { return account; }
	public SecurityItem getSecurityItem() { return securityItem; }
	public DividendEvent getDividendEvent() { return dividendEvent; }
	public BigDecimal getQuantityAtRecordDate() { return quantityAtRecordDate; }
	public BigDecimal getDividendPerShare() { return dividendPerShare; }
	public BigDecimal getGrossAmount() { return grossAmount; }
	public BigDecimal getTaxAmount() { return taxAmount; }
	public BigDecimal getNetAmount() { return netAmount; }
	public String getCurrency() { return currency; }
	public BigDecimal getExchangeRate() { return exchangeRate; }
	public BigDecimal getGrossAmountKrw() { return grossAmountKrw; }
	public BigDecimal getNetAmountKrw() { return netAmountKrw; }
	public LocalDate getPaymentDate() { return paymentDate; }
	public DividendPaymentStatus getStatus() { return status; }
}
