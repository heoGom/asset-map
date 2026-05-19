package com.assetmap.backend.datasync.provider;

import java.time.LocalDate;
import java.util.List;

public interface SecurityMasterProvider {

	default List<ImportedSecurityMaster> fetchAllKospi() {
		return fetchAllKospi(LocalDate.now());
	}

	default List<ImportedSecurityMaster> fetchAllKosdaq() {
		return fetchAllKosdaq(LocalDate.now());
	}

	default List<ImportedSecurityMaster> fetchAllEtf() {
		return fetchAllEtf(LocalDate.now());
	}

	List<ImportedSecurityMaster> fetchAllKospi(LocalDate basDd);

	List<ImportedSecurityMaster> fetchAllKosdaq(LocalDate basDd);

	List<ImportedSecurityMaster> fetchAllEtf(LocalDate basDd);

}
