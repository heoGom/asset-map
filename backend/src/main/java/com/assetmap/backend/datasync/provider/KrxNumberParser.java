package com.assetmap.backend.datasync.provider;

import java.math.BigDecimal;
import org.springframework.util.StringUtils;

public final class KrxNumberParser {

	private KrxNumberParser() {
	}

	public static BigDecimal decimal(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		String normalized = value.replace(",", "").trim();
		if (normalized.isBlank() || "-".equals(normalized)) {
			return null;
		}
		return new BigDecimal(normalized);
	}

	public static Long longValue(String value) {
		BigDecimal decimal = decimal(value);
		return decimal == null ? null : decimal.longValue();
	}
}
