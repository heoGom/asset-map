package com.assetmap.backend.dividend;

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
public class DividendEvent extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "security_item_id", nullable = false)
	private SecurityItem securityItem;

	@Column(nullable = false)
	private Integer dividendYear;

	private LocalDate exDividendDate;
	private LocalDate paymentDate;

	@Column(nullable = false, precision = 19, scale = 6)
	private BigDecimal dividendPerShare;

	@Column(nullable = false)
	private String currency;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DataSourceType source;

	protected DividendEvent() {
	}

	public DividendEvent(SecurityItem securityItem, Integer dividendYear, LocalDate exDividendDate, LocalDate paymentDate, BigDecimal dividendPerShare, String currency, DataSourceType source) {
		this.securityItem = securityItem;
		this.dividendYear = dividendYear;
		this.exDividendDate = exDividendDate;
		this.paymentDate = paymentDate;
		this.dividendPerShare = dividendPerShare;
		this.currency = currency;
		this.source = source;
	}

	public void update(DividendEventUpdateRequest request, SecurityItem securityItem) {
		if (securityItem != null) this.securityItem = securityItem;
		if (request.dividendYear() != null) this.dividendYear = request.dividendYear();
		if (request.exDividendDate() != null) this.exDividendDate = request.exDividendDate();
		if (request.paymentDate() != null) this.paymentDate = request.paymentDate();
		if (request.dividendPerShare() != null) this.dividendPerShare = request.dividendPerShare();
		if (request.currency() != null) this.currency = request.currency();
		if (request.source() != null) this.source = request.source();
	}

	public Long getId() { return id; }
	public SecurityItem getSecurityItem() { return securityItem; }
	public Integer getDividendYear() { return dividendYear; }
	public LocalDate getExDividendDate() { return exDividendDate; }
	public LocalDate getPaymentDate() { return paymentDate; }
	public BigDecimal getDividendPerShare() { return dividendPerShare; }
	public String getCurrency() { return currency; }
	public DataSourceType getSource() { return source; }
}
