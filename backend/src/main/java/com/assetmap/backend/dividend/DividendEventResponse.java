package com.assetmap.backend.dividend;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DividendEventResponse(
		Long id,
		Long securityItemId,
		String ticker,
		String securityName,
		Integer dividendYear,
		LocalDate declarationDate,
		LocalDate exDividendDate,
		LocalDate recordDate,
		LocalDate paymentDate,
		DividendEventType eventType,
		BigDecimal dividendPerShare,
		String currency,
		DataSourceType source,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {

	public static DividendEventResponse from(DividendEvent event) {
		return new DividendEventResponse(
				event.getId(),
				event.getSecurityItem().getId(),
				event.getSecurityItem().getTicker(),
				event.getSecurityItem().getName(),
				event.getDividendYear(),
				event.getDeclarationDate(),
				event.getExDividendDate(),
				event.getRecordDate(),
				event.getPaymentDate(),
				event.getEventType(),
				event.getDividendPerShare(),
				event.getCurrency(),
				event.getSource(),
				event.getCreatedAt(),
				event.getUpdatedAt()
		);
	}
}
