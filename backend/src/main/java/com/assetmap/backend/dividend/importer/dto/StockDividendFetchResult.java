package com.assetmap.backend.dividend.importer.dto;

import java.util.List;

public record StockDividendFetchResult(
		String searchTerm,
		int httpStatus,
		String resultCode,
		String resultMsg,
		int totalCount,
		List<ImportedDividendEvent> items
) {

	public int itemCount() {
		return items == null ? 0 : items.size();
	}
}
