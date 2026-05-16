package com.assetmap.backend.securityitem;

import com.assetmap.backend.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class SecurityItem extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String ticker;

	@Column(nullable = false)
	private String name;

	private String market;
	private String country;

	@Column(nullable = false)
	private String currency;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SecurityType securityType;

	protected SecurityItem() {
	}

	public SecurityItem(String ticker, String name, String market, String country, String currency, SecurityType securityType) {
		this.ticker = ticker;
		this.name = name;
		this.market = market;
		this.country = country;
		this.currency = currency;
		this.securityType = securityType;
	}

	public void update(SecurityItemUpdateRequest request) {
		if (request.ticker() != null) this.ticker = request.ticker();
		if (request.name() != null) this.name = request.name();
		if (request.market() != null) this.market = request.market();
		if (request.country() != null) this.country = request.country();
		if (request.currency() != null) this.currency = request.currency();
		if (request.securityType() != null) this.securityType = request.securityType();
	}

	public Long getId() {
		return id;
	}

	public String getTicker() {
		return ticker;
	}

	public String getName() {
		return name;
	}

	public String getMarket() {
		return market;
	}

	public String getCountry() {
		return country;
	}

	public String getCurrency() {
		return currency;
	}

	public SecurityType getSecurityType() {
		return securityType;
	}
}
