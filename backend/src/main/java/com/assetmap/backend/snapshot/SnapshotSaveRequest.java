package com.assetmap.backend.snapshot;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record SnapshotSaveRequest(
		@NotNull Long userId,
		LocalDate snapshotDate
) {
}
