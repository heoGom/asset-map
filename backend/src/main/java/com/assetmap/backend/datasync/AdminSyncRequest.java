package com.assetmap.backend.datasync;

import java.time.LocalDate;

public record AdminSyncRequest(
		Boolean force,
		LocalDate priceDate,
		LocalDate basDd,
		Integer fromYear,
		Integer toYear
) {

	public boolean forceOrFalse() {
		return Boolean.TRUE.equals(force);
	}
}
