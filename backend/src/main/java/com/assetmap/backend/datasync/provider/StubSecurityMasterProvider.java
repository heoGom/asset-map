package com.assetmap.backend.datasync.provider;

import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StubSecurityMasterProvider implements SecurityMasterProvider {

	private static final Logger log = LoggerFactory.getLogger(StubSecurityMasterProvider.class);

	@Override
	public List<ImportedSecurityMaster> fetchAllKospi(LocalDate basDd) {
		log.info("KRX security master provider is not implemented yet. market=KOSPI");
		return List.of();
	}

	@Override
	public List<ImportedSecurityMaster> fetchAllKosdaq(LocalDate basDd) {
		log.info("KRX security master provider is not implemented yet. market=KOSDAQ");
		return List.of();
	}

	@Override
	public List<ImportedSecurityMaster> fetchAllEtf(LocalDate basDd) {
		log.info("KRX security master provider is not implemented yet. market=ETF");
		return List.of();
	}
}
