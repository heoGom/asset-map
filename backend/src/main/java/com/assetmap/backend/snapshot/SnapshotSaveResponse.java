package com.assetmap.backend.snapshot;

import java.time.LocalDate;

public record SnapshotSaveResponse(
		LocalDate snapshotDate,
		int savedCount
) {
}
