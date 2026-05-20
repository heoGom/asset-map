package com.assetmap.backend.transaction;

import java.time.LocalDate;

public interface SecurityTradeStartProjection {

	Long getSecurityItemId();

	LocalDate getFirstTradeDate();
}
