package com.assetmap.backend.holding;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyCalculator {

	public static final int MONEY_SCALE = 2;
	public static final int RATE_SCALE = 2;
	public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);

	private MoneyCalculator() {
	}

	public static BigDecimal amount(BigDecimal quantity, BigDecimal price) {
		return quantity.multiply(price).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
	}

	public static BigDecimal rate(BigDecimal numerator, BigDecimal denominator) {
		if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO.setScale(RATE_SCALE, RoundingMode.HALF_UP);
		}
		return numerator
				.multiply(BigDecimal.valueOf(100))
				.divide(denominator, RATE_SCALE, RoundingMode.HALF_UP);
	}
}
