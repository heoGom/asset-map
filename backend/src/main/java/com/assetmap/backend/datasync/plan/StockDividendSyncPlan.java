package com.assetmap.backend.datasync.plan;

import com.assetmap.backend.securityitem.SecurityItem;
import java.util.List;

public record StockDividendSyncPlan(List<SecurityItem> targetSecurities, YearRange range, List<StockDividendYearTarget> yearTargets) {
}
