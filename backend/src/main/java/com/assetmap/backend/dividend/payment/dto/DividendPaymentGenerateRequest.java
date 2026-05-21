package com.assetmap.backend.dividend.payment.dto;

import jakarta.validation.constraints.NotNull;

public record DividendPaymentGenerateRequest(
		@NotNull Long userId,
		@NotNull Long dividendEventId
) {
}
