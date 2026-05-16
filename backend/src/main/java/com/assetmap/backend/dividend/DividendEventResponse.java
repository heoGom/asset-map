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
		LocalDate exDividendDate,
		LocalDate paymentDate,
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
				event.getExDividendDate(),
				event.getPaymentDate(),
				event.getDividendPerShare(),
				event.getCurrency(),
				event.getSource(),
				event.getCreatedAt(),
				event.getUpdatedAt()
		);
	}
}
