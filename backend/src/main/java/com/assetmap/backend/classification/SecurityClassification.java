package com.assetmap.backend.classification;

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
import jakarta.persistence.OneToOne;

@Entity
public class SecurityClassification extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "security_item_id", nullable = false, unique = true)
	private SecurityItem securityItem;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CountryGroup countryGroup;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AssetGroup assetGroup;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Sector sector;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StrategyType strategyType;

	private String theme;

	protected SecurityClassification() {
	}

	public SecurityClassification(
			SecurityItem securityItem,
			CountryGroup countryGroup,
			AssetGroup assetGroup,
			Sector sector,
			StrategyType strategyType,
			String theme
	) {
		this.securityItem = securityItem;
		this.countryGroup = countryGroup;
		this.assetGroup = assetGroup;
		this.sector = sector;
		this.strategyType = strategyType;
		this.theme = theme;
	}

	public void update(SecurityClassificationUpdateRequest request) {
		if (request.countryGroup() != null) this.countryGroup = request.countryGroup();
		if (request.assetGroup() != null) this.assetGroup = request.assetGroup();
		if (request.sector() != null) this.sector = request.sector();
		if (request.strategyType() != null) this.strategyType = request.strategyType();
		if (request.theme() != null) this.theme = request.theme();
	}

	public Long getId() {
		return id;
	}

	public SecurityItem getSecurityItem() {
		return securityItem;
	}

	public CountryGroup getCountryGroup() {
		return countryGroup;
	}

	public AssetGroup getAssetGroup() {
		return assetGroup;
	}

	public Sector getSector() {
		return sector;
	}

	public StrategyType getStrategyType() {
		return strategyType;
	}

	public String getTheme() {
		return theme;
	}
}
