package com.assetmap.backend.dividend.importer.provider;

import com.assetmap.backend.dividend.importer.dto.ImportedDividendEvent;
import com.assetmap.backend.securityitem.SecurityItem;
import java.util.List;

public interface StockDividendProvider {

	List<ImportedDividendEvent> fetch(SecurityItem securityItem);
}
