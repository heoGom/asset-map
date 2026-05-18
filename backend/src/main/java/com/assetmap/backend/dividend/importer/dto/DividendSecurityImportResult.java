package com.assetmap.backend.dividend.importer.dto;

import java.util.List;

public record DividendSecurityImportResult(
		Long securityItemId,
		String securityName,
		List<String> searchTerms,
		Integer httpStatus,
		String resultCode,
		String resultMsg,
		int totalCount,
		int itemCount,
		int importedCount,
		int skippedCount,
		int generatedPaymentCount,
		String status,
		String message,
		List<DividendSkipSummary> skipReasons
) {
}
