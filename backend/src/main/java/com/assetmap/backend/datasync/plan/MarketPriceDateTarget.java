package com.assetmap.backend.datasync.plan;

import com.assetmap.backend.securityitem.SecurityItem;
import java.time.LocalDate;
import java.util.List;

public record MarketPriceDateTarget(LocalDate priceDate, List<SecurityItem> targetSecurities) {
}
