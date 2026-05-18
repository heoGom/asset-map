package com.assetmap.backend.dividend.importer.service;

import com.assetmap.backend.dividend.importer.dto.DividendImportSkipReason;

public class DividendEventImportOperationException extends RuntimeException {

	private final DividendImportSkipReason reason;

	public DividendEventImportOperationException(DividendImportSkipReason reason, Throwable cause) {
		super(reason.name(), cause);
		this.reason = reason;
	}

	public DividendImportSkipReason getReason() {
		return reason;
	}
}
