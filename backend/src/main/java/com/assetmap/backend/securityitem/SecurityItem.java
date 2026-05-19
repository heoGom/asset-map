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

	@Column(nullable = false, unique = true)
	private String ticker;

	private String isinCode;

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
		this(ticker, null, name, market, country, currency, securityType);
	}

	public SecurityItem(String ticker, String isinCode, String name, String market, String country, String currency, SecurityType securityType) {
		this.ticker = ticker;
		this.isinCode = isinCode;
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

	public void updateMasterData(String ticker, String isinCode, String name, String market, String country, String currency, SecurityType securityType) {
		if (ticker != null) this.ticker = ticker;
		if (isinCode != null) this.isinCode = isinCode;
		if (name != null) this.name = name;
		if (market != null) this.market = market;
		if (country != null) this.country = country;
		if (currency != null) this.currency = currency;
		if (securityType != null) this.securityType = securityType;
	}

	public Long getId() {
		return id;
	}

	public String getTicker() {
		return ticker;
	}

	public String getIsinCode() {
		return isinCode;
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
