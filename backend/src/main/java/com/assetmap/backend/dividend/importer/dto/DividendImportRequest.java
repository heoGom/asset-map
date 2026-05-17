package com.assetmap.backend.dividend.importer.dto;

import jakarta.validation.constraints.Min;

public record DividendImportRequest(
		Long securityItemId,
		@Min(2000) Integer fromYear,
		@Min(2000) Integer toYear
) {
}
