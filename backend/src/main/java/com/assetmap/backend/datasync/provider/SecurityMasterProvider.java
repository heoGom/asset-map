package com.assetmap.backend.datasync.provider;

import java.util.List;

public interface SecurityMasterProvider {

	List<ImportedSecurityMaster> fetchAllKospi();

	List<ImportedSecurityMaster> fetchAllKosdaq();

	List<ImportedSecurityMaster> fetchAllEtf();
}
