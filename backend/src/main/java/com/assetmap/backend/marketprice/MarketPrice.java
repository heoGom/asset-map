package com.assetmap.backend.marketprice;

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
import java.time.LocalDateTime;

@Entity
public class MarketPrice extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "security_item_id", nullable = false)
	private SecurityItem securityItem;

	@Column(nullable = false)
	private LocalDate priceDate;

	@Column(nullable = false, precision = 19, scale = 6)
	private BigDecimal closePrice;

	@Column(nullable = false, precision = 19, scale = 6)
	private BigDecimal currentPrice;

	@Column(nullable = false, precision = 19, scale = 6)
	private BigDecimal changeAmount;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal changeRate;

	private Long volume;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MarketDataSource source;

	@Column(nullable = false)
	private LocalDateTime fetchedAt;

	protected MarketPrice() {
	}

	public MarketPrice(SecurityItem securityItem, LocalDate priceDate, BigDecimal closePrice, BigDecimal currentPrice, BigDecimal changeAmount, BigDecimal changeRate, Long volume, MarketDataSource source, LocalDateTime fetchedAt) {
		this.securityItem = securityItem;
		this.priceDate = priceDate;
		this.closePrice = closePrice;
		this.currentPrice = currentPrice;
		this.changeAmount = changeAmount;
		this.changeRate = changeRate;
		this.volume = volume;
		this.source = source;
		this.fetchedAt = fetchedAt == null ? LocalDateTime.now() : fetchedAt;
	}

	public void update(BigDecimal closePrice, BigDecimal currentPrice, BigDecimal changeAmount, BigDecimal changeRate, Long volume, LocalDateTime fetchedAt) {
		this.closePrice = closePrice;
		this.currentPrice = currentPrice;
		this.changeAmount = changeAmount;
		this.changeRate = changeRate;
		this.volume = volume;
		this.fetchedAt = fetchedAt == null ? LocalDateTime.now() : fetchedAt;
	}

	public Long getId() { return id; }
	public SecurityItem getSecurityItem() { return securityItem; }
	public LocalDate getPriceDate() { return priceDate; }
	public BigDecimal getClosePrice() { return closePrice; }
	public BigDecimal getCurrentPrice() { return currentPrice; }
	public BigDecimal getChangeAmount() { return changeAmount; }
	public BigDecimal getChangeRate() { return changeRate; }
	public Long getVolume() { return volume; }
	public MarketDataSource getSource() { return source; }
	public LocalDateTime getFetchedAt() { return fetchedAt; }
}
