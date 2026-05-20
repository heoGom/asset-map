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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
		name = "dividend_event",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_dividend_event_security_record_amount_source",
				columnNames = {"security_item_id", "record_date", "dividend_per_share", "source"}
		)
)
public class DividendEvent extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "security_item_id", nullable = false)
	private SecurityItem securityItem;

	@Column(nullable = false)
	private Integer dividendYear;

	private LocalDate declarationDate;
	private LocalDate exDividendDate;
	@Column(nullable = false)
	private LocalDate recordDate;
	private LocalDate paymentDate;

	@Enumerated(EnumType.STRING)
	private DividendEventType eventType;

	@Column(nullable = false, precision = 19, scale = 6)
	private BigDecimal dividendPerShare;

	@Column(nullable = false)
	private String currency;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DataSourceType source;

	protected DividendEvent() {
	}

	public DividendEvent(SecurityItem securityItem, Integer dividendYear, LocalDate declarationDate, LocalDate exDividendDate, LocalDate recordDate, LocalDate paymentDate, DividendEventType eventType, BigDecimal dividendPerShare, String currency, DataSourceType source) {
		this.securityItem = securityItem;
		this.dividendYear = dividendYear;
		this.declarationDate = declarationDate;
		this.exDividendDate = exDividendDate;
		this.recordDate = recordDate;
		this.paymentDate = paymentDate;
		this.eventType = eventType;
		this.dividendPerShare = dividendPerShare;
		this.currency = currency;
		this.source = source;
	}

	public void update(DividendEventUpdateRequest request, SecurityItem securityItem) {
		if (securityItem != null) this.securityItem = securityItem;
		if (request.dividendYear() != null) this.dividendYear = request.dividendYear();
		if (request.declarationDate() != null) this.declarationDate = request.declarationDate();
		if (request.exDividendDate() != null) this.exDividendDate = request.exDividendDate();
		if (request.recordDate() != null) this.recordDate = request.recordDate();
		if (request.paymentDate() != null) this.paymentDate = request.paymentDate();
		if (request.eventType() != null) this.eventType = request.eventType();
		if (request.dividendPerShare() != null) this.dividendPerShare = request.dividendPerShare();
		if (request.currency() != null) this.currency = request.currency();
		if (request.source() != null) this.source = request.source();
	}

	public Long getId() { return id; }
	public SecurityItem getSecurityItem() { return securityItem; }
	public Integer getDividendYear() { return dividendYear; }
	public LocalDate getDeclarationDate() { return declarationDate; }
	public LocalDate getExDividendDate() { return exDividendDate; }
	public LocalDate getRecordDate() { return recordDate; }
	public LocalDate getPaymentDate() { return paymentDate; }
	public DividendEventType getEventType() { return eventType; }
	public BigDecimal getDividendPerShare() { return dividendPerShare; }
	public String getCurrency() { return currency; }
	public DataSourceType getSource() { return source; }
}
