package com.assetmap.backend.datasync.provider;

import com.assetmap.backend.datasync.DataSyncSource;
import com.assetmap.backend.securityitem.SecurityType;
import java.time.LocalDate;

public record ImportedSecurityMaster(
		String ticker,
		String isinCode,
		String name,
		String shortName,
		String englishName,
		String market,
		SecurityType securityType,
		LocalDate listingDate,
		String currency,
		DataSyncSource source
) {
}
