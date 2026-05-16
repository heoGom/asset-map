package com.assetmap.backend.snapshot;

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
import java.time.LocalDate;

@Entity
public class HoldingSnapshot extends BaseEntity {

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

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal evaluatedAmount;

	@Column(nullable = false)
	private LocalDate snapshotDate;

	@Column(nullable = false)
	private String currency;

	@Column(nullable = false, precision = 19, scale = 6)
	private BigDecimal exchangeRate;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal evaluatedAmountKrw;

	protected HoldingSnapshot() {
	}

	public HoldingSnapshot(
			Long userId,
			Account account,
			SecurityItem securityItem,
			BigDecimal quantity,
			BigDecimal averagePrice,
			BigDecimal currentPrice,
			BigDecimal evaluatedAmount,
			LocalDate snapshotDate,
			String currency,
			BigDecimal exchangeRate,
			BigDecimal evaluatedAmountKrw
	) {
		this.userId = userId;
		this.account = account;
		this.securityItem = securityItem;
		this.quantity = quantity;
		this.averagePrice = averagePrice;
		this.currentPrice = currentPrice;
		this.evaluatedAmount = evaluatedAmount;
		this.snapshotDate = snapshotDate;
		this.currency = currency;
		this.exchangeRate = exchangeRate;
		this.evaluatedAmountKrw = evaluatedAmountKrw;
	}

	public Long getUserId() {
		return userId;
	}

	public Account getAccount() {
		return account;
	}

	public LocalDate getSnapshotDate() {
		return snapshotDate;
	}

	public BigDecimal getEvaluatedAmountKrw() {
		return evaluatedAmountKrw;
	}
}
