package com.assetmap.backend.datasync.execution;
import com.assetmap.backend.datasync.execution.SyncUpsertResult;

import com.assetmap.backend.datasync.provider.ImportedSecurityMaster;
import com.assetmap.backend.securityitem.SecurityItem;
import com.assetmap.backend.securityitem.SecurityItemRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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
	 * KRX 종목 마스터는 전체 상장 종목 수집 대상이다. 영문명, 상장일 등 확장 속성은
	 * DB 컬럼을 한 번에 늘리지 않고 추후 필요 시 추가한다.
	 */
	@Transactional
	public SyncUpsertResult upsertImportedSecurities(List<ImportedSecurityMaster> importedSecurities) {
		int inserted = 0;
		int updated = 0;
		int skipped = 0;
		Map<String, SecurityItem> existingByTicker = securityItemRepository.findByTickerIn(
				importedSecurities.stream()
						.map(ImportedSecurityMaster::ticker)
						.filter(StringUtils::hasText)
						.collect(Collectors.toSet())
		).stream().collect(Collectors.toMap(SecurityItem::getTicker, Function.identity(), (left, right) -> left));

		for (ImportedSecurityMaster imported : importedSecurities) {
			if (!StringUtils.hasText(imported.ticker()) || !StringUtils.hasText(imported.name()) || imported.securityType() == null) {
				skipped++;
				continue;
			}
			SecurityItem securityItem = existingByTicker.get(imported.ticker());
			if (securityItem == null) {
				SecurityItem saved = securityItemRepository.save(new SecurityItem(
						imported.ticker(),
						blankToNull(imported.isinCode()),
						imported.name(),
						imported.market(),
						"KOREA",
						StringUtils.hasText(imported.currency()) ? imported.currency() : "KRW",
						imported.securityType()
				));
				existingByTicker.put(saved.getTicker(), saved);
				inserted++;
				continue;
			}
			securityItem.updateMasterData(
					imported.ticker(),
					blankToNull(imported.isinCode()),
					imported.name(),
					imported.market(),
					"KOREA",
					StringUtils.hasText(imported.currency()) ? imported.currency() : "KRW",
					imported.securityType()
			);
			updated++;
		}

		return new SyncUpsertResult(importedSecurities.size(), inserted, updated, skipped);
	}

	private String blankToNull(String value) {
		return StringUtils.hasText(value) ? value : null;
	}
}
