package com.assetmap.backend.dividend.importer.dto;

import java.util.List;

public record DividendImportResult(
		int targetSecurityCount,
		int importedEventCount,
		int skippedEventCount,
		int generatedPaymentCount,
		int failedSecurityCount,
		List<DividendSecurityImportResult> securities
) {

	public static DividendImportResult empty() {
		return new DividendImportResult(0, 0, 0, 0, 0, List.of());
	}

	public DividendImportResult plus(DividendImportResult other) {
		return new DividendImportResult(
				targetSecurityCount + other.targetSecurityCount,
				importedEventCount + other.importedEventCount,
				skippedEventCount + other.skippedEventCount,
				generatedPaymentCount + other.generatedPaymentCount,
				failedSecurityCount + other.failedSecurityCount,
				concat(securities, other.securities)
		);
	}

	private static List<DividendSecurityImportResult> concat(
			List<DividendSecurityImportResult> first,
			List<DividendSecurityImportResult> second
	) {
		return java.util.stream.Stream.concat(first.stream(), second.stream()).toList();
	}
}
