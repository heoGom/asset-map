package com.assetmap.backend.datasync.plan;

import com.assetmap.backend.securityitem.SecurityItem;
import java.util.List;

public record MarketPriceSyncPlan(List<SecurityItem> targetSecurities, List<MarketPriceDateTarget> dateTargets) {
}
