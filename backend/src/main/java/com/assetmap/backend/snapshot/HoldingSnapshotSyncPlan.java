package com.assetmap.backend.snapshot;

import java.time.LocalDate;
import java.util.List;

public record HoldingSnapshotSyncPlan(List<LocalDate> snapshotDates) {
}
