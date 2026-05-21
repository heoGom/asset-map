package com.assetmap.backend.datasync.admin.dto;

import java.time.LocalDate;

public record AdminSyncRequest(
		Boolean force,
		LocalDate priceDate,
		LocalDate basDd,
		LocalDate fromDate,
		LocalDate toDate,
		Integer maxDates,
		Integer fromYear,
		Integer toYear
) {

	public AdminSyncRequest(Boolean force, LocalDate priceDate, LocalDate basDd, Integer fromYear, Integer toYear) {
		this(force, priceDate, basDd, null, null, null, fromYear, toYear);
	}

	public boolean forceOrFalse() {
		return Boolean.TRUE.equals(force);
	}
}
