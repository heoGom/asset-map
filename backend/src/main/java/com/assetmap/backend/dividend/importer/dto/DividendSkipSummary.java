package com.assetmap.backend.dividend.importer.dto;

public record DividendSkipSummary(
		DividendImportSkipReason reason,
		int count
) {
}
