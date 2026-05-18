package com.assetmap.backend.datasync;

import com.assetmap.backend.datasync.provider.ImportedSecurityMaster;
import com.assetmap.backend.securityitem.SecurityItem;
import com.assetmap.backend.securityitem.SecurityItemRepository;
import com.assetmap.backend.securityitem.SecurityItemUpdateRequest;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class SecurityMasterSyncService {

	private final SecurityItemRepository securityItemRepository;

	public SecurityMasterSyncService(SecurityItemRepository securityItemRepository) {
		this.securityItemRepository = securityItemRepository;
	}

	/**
	 * KRX 종목 마스터는 전체 상장 종목 수집 대상이다. 현재 DB에는 isinCode 컬럼이 없으므로
	 * ticker/name/market/securityType/currency 중심으로 upsert하고 isinCode 확장은 TODO로 남긴다.
	 */
	@Transactional
	public SyncUpsertResult upsertImportedSecurities(List<ImportedSecurityMaster> importedSecurities) {
		int inserted = 0;
		int updated = 0;
		int skipped = 0;

		for (ImportedSecurityMaster imported : importedSecurities) {
			if (!StringUtils.hasText(imported.ticker()) || !StringUtils.hasText(imported.name()) || imported.securityType() == null) {
				skipped++;
				continue;
			}
			SecurityItem securityItem = securityItemRepository.findByTicker(imported.ticker())
					.orElse(null);
			if (securityItem == null) {
				securityItemRepository.save(new SecurityItem(
						imported.ticker(),
						imported.name(),
						imported.market(),
						"KOREA",
						StringUtils.hasText(imported.currency()) ? imported.currency() : "KRW",
						imported.securityType()
				));
				inserted++;
				continue;
			}
			securityItem.update(new SecurityItemUpdateRequest(
					imported.ticker(),
					imported.name(),
					imported.market(),
					null,
					StringUtils.hasText(imported.currency()) ? imported.currency() : "KRW",
					imported.securityType()
			));
			updated++;
		}

		return new SyncUpsertResult(importedSecurities.size(), inserted, updated, skipped);
	}
}
