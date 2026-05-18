package com.assetmap.backend.dividend.importer.provider;

import com.assetmap.backend.dividend.importer.dto.StockDividendFetchResult;

public interface StockDividendProvider {

	StockDividendFetchResult fetch(String searchTerm);
}
