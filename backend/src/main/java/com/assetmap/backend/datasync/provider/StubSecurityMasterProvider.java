package com.assetmap.backend.datasync.provider;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StubSecurityMasterProvider implements SecurityMasterProvider {

	private static final Logger log = LoggerFactory.getLogger(StubSecurityMasterProvider.class);

	@Override
	public List<ImportedSecurityMaster> fetchAllKospi() {
		log.info("KRX security master provider is not implemented yet. market=KOSPI");
		return List.of();
	}

	@Override
	public List<ImportedSecurityMaster> fetchAllKosdaq() {
		log.info("KRX security master provider is not implemented yet. market=KOSDAQ");
		return List.of();
	}

	@Override
	public List<ImportedSecurityMaster> fetchAllEtf() {
		log.info("KRX security master provider is not implemented yet. market=ETF");
		return List.of();
	}
}
