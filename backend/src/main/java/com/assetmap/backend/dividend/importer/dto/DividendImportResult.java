package com.assetmap.backend.dividend.importer.dto;

public record DividendImportResult(
		int targetSecurityCount,
		int importedEventCount,
		int skippedEventCount,
		int generatedPaymentCount,
		int failedSecurityCount
) {

	public static DividendImportResult empty() {
		return new DividendImportResult(0, 0, 0, 0, 0);
	}

	public DividendImportResult plus(DividendImportResult other) {
		return new DividendImportResult(
				targetSecurityCount + other.targetSecurityCount,
				importedEventCount + other.importedEventCount,
				skippedEventCount + other.skippedEventCount,
				generatedPaymentCount + other.generatedPaymentCount,
				failedSecurityCount + other.failedSecurityCount
		);
	}
}
