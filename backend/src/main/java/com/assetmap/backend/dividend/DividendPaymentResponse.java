package com.assetmap.backend.dividend;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DividendPaymentResponse(
		Long id,
		Long userId,
		Long accountId,
		String accountName,
		Long securityItemId,
		String ticker,
		String securityName,
		Long dividendEventId,
		BigDecimal quantityAtRecordDate,
		BigDecimal dividendPerShare,
		BigDecimal grossAmount,
		BigDecimal taxAmount,
		BigDecimal netAmount,
		String currency,
		BigDecimal exchangeRate,
		BigDecimal grossAmountKrw,
		BigDecimal netAmountKrw,
		LocalDate paymentDate,
		DividendPaymentStatus status,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {

	public static DividendPaymentResponse from(DividendPayment payment) {
		return new DividendPaymentResponse(
				payment.getId(),
				payment.getUserId(),
				payment.getAccount().getId(),
				payment.getAccount().getName(),
				payment.getSecurityItem().getId(),
				payment.getSecurityItem().getTicker(),
				payment.getSecurityItem().getName(),
				payment.getDividendEvent() == null ? null : payment.getDividendEvent().getId(),
				payment.getQuantityAtRecordDate(),
				payment.getDividendPerShare(),
				payment.getGrossAmount(),
				payment.getTaxAmount(),
				payment.getNetAmount(),
				payment.getCurrency(),
				payment.getExchangeRate(),
				payment.getGrossAmountKrw(),
				payment.getNetAmountKrw(),
				payment.getPaymentDate(),
				payment.getStatus(),
				payment.getCreatedAt(),
				payment.getUpdatedAt()
		);
	}
}
