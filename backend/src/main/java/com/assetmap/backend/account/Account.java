package com.assetmap.backend.account;

import com.assetmap.backend.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Account extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false)
	private String name;

	private String brokerName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AccountType accountType;

	@Column(nullable = false)
	private String currency;

	private String memo;

	protected Account() {
	}

	public Account(Long userId, String name, String brokerName, AccountType accountType, String currency, String memo) {
		this.userId = userId;
		this.name = name;
		this.brokerName = brokerName;
		this.accountType = accountType;
		this.currency = currency;
		this.memo = memo;
	}

	public void update(AccountUpdateRequest request) {
		if (request.name() != null) {
			this.name = request.name();
		}
		if (request.brokerName() != null) {
			this.brokerName = request.brokerName();
		}
		if (request.accountType() != null) {
			this.accountType = request.accountType();
		}
		if (request.currency() != null) {
			this.currency = request.currency();
		}
		if (request.memo() != null) {
			this.memo = request.memo();
		}
	}

	public Long getId() {
		return id;
	}

	public Long getUserId() {
		return userId;
	}

	public String getName() {
		return name;
	}

	public String getBrokerName() {
		return brokerName;
	}

	public AccountType getAccountType() {
		return accountType;
	}

	public String getCurrency() {
		return currency;
	}

	public String getMemo() {
		return memo;
	}
}
