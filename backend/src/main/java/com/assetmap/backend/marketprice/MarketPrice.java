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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
		name = "market_price",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_market_price_security_date_source",
				columnNames = {"security_item_id", "price_date", "source"}
		)
)
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

	@Column(precision = 19, scale = 6)
	private BigDecimal openPrice;

	@Column(precision = 19, scale = 6)
	private BigDecimal highPrice;

	@Column(precision = 19, scale = 6)
	private BigDecimal lowPrice;

	@Column(precision = 28, scale = 2)
	private BigDecimal tradingValue;

	@Column(precision = 28, scale = 2)
	private BigDecimal marketCap;

	@Column(precision = 19, scale = 6)
	private BigDecimal nav;

	private String underlyingIndexName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MarketDataSource source;

	@Column(nullable = false)
	private LocalDateTime fetchedAt;

	protected MarketPrice() {
	}

	public MarketPrice(SecurityItem securityItem, LocalDate priceDate, BigDecimal closePrice, BigDecimal currentPrice, BigDecimal changeAmount, BigDecimal changeRate, Long volume, MarketDataSource source, LocalDateTime fetchedAt) {
		this(securityItem, priceDate, closePrice, currentPrice, changeAmount, changeRate, volume, null, null, null, null, null, null, null, source, fetchedAt);
	}

	public MarketPrice(
			SecurityItem securityItem,
			LocalDate priceDate,
			BigDecimal closePrice,
			BigDecimal currentPrice,
			BigDecimal changeAmount,
			BigDecimal changeRate,
			Long volume,
			BigDecimal openPrice,
			BigDecimal highPrice,
			BigDecimal lowPrice,
			BigDecimal tradingValue,
			BigDecimal marketCap,
			BigDecimal nav,
			String underlyingIndexName,
			MarketDataSource source,
			LocalDateTime fetchedAt
	) {
		this.securityItem = securityItem;
		this.priceDate = priceDate;
		this.closePrice = closePrice;
		this.currentPrice = currentPrice;
		this.changeAmount = changeAmount;
		this.changeRate = changeRate;
		this.volume = volume;
		this.openPrice = openPrice;
		this.highPrice = highPrice;
		this.lowPrice = lowPrice;
		this.tradingValue = tradingValue;
		this.marketCap = marketCap;
		this.nav = nav;
		this.underlyingIndexName = underlyingIndexName;
		this.source = source;
		this.fetchedAt = fetchedAt == null ? LocalDateTime.now() : fetchedAt;
	}

	public void update(BigDecimal closePrice, BigDecimal currentPrice, BigDecimal changeAmount, BigDecimal changeRate, Long volume, LocalDateTime fetchedAt) {
		update(closePrice, currentPrice, changeAmount, changeRate, volume, null, null, null, null, null, null, null, fetchedAt);
	}

	public void update(
			BigDecimal closePrice,
			BigDecimal currentPrice,
			BigDecimal changeAmount,
			BigDecimal changeRate,
			Long volume,
			BigDecimal openPrice,
			BigDecimal highPrice,
			BigDecimal lowPrice,
			BigDecimal tradingValue,
			BigDecimal marketCap,
			BigDecimal nav,
			String underlyingIndexName,
			LocalDateTime fetchedAt
	) {
		this.closePrice = closePrice;
		this.currentPrice = currentPrice;
		this.changeAmount = changeAmount;
		this.changeRate = changeRate;
		this.volume = volume;
		this.openPrice = openPrice;
		this.highPrice = highPrice;
		this.lowPrice = lowPrice;
		this.tradingValue = tradingValue;
		this.marketCap = marketCap;
		this.nav = nav;
		this.underlyingIndexName = underlyingIndexName;
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
	public BigDecimal getOpenPrice() { return openPrice; }
	public BigDecimal getHighPrice() { return highPrice; }
	public BigDecimal getLowPrice() { return lowPrice; }
	public BigDecimal getTradingValue() { return tradingValue; }
	public BigDecimal getMarketCap() { return marketCap; }
	public BigDecimal getNav() { return nav; }
	public String getUnderlyingIndexName() { return underlyingIndexName; }
	public MarketDataSource getSource() { return source; }
	public LocalDateTime getFetchedAt() { return fetchedAt; }
}
