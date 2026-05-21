package com.assetmap.backend.datasync.common;

public record DataSyncDecision(
		boolean shouldRun,
		String message
) {

	public static DataSyncDecision run(String message) {
		return new DataSyncDecision(true, message);
	}

	public static DataSyncDecision skip(String message) {
		return new DataSyncDecision(false, message);
	}
}
