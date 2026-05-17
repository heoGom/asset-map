package com.assetmap.backend.dividend.importer.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ImportedDividendEvent(
		String isinCode,
		String isinName,
		String companyName,
		String stockTypeName,
		String dividendRecordName,
		LocalDate recordDate,
		LocalDate paymentDate,
		BigDecimal dividendPerShare
) {
}
