package com.assetmap.backend.datasync.plan;

import com.assetmap.backend.securityitem.SecurityItem;

public record StockDividendYearTarget(SecurityItem securityItem, int year) {
}
