package com.assetmap.backend.datasync;

import com.assetmap.backend.datasync.provider.ImportedMarketPrice;
import com.assetmap.backend.holding.HoldingRepository;
import com.assetmap.backend.marketprice.MarketDataSource;
import com.assetmap.backend.marketprice.MarketPrice;
import com.assetmap.backend.marketprice.MarketPriceRepository;
import com.assetmap.backend.securityitem.SecurityItem;
import com.assetmap.backend.securityitem.SecurityItemRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class MarketPriceSyncService {

	private final SecurityItemRepository securityItemRepository;
	private final MarketPriceRepository marketPriceRepository;
	private final HoldingRepository holdingRepository;

	public MarketPriceSyncService(
			SecurityItemRepository securityItemRepository,
			MarketPriceRepository marketPriceRepository,
			HoldingRepository holdingRepository
	) {
		this.securityItemRepository = securityItemRepository;
		this.marketPriceRepository = marketPriceRepository;
		this.holdingRepository = holdingRepository;
	}

	/**
	 * KRX 시세 원천은 전체 시장 단위로 내려받을 수 있지만, 저장은 현재 DB에 존재하는 종목만 수행한다.
	 * 관심 종목 도메인이 생기면 보유/거래/관심 종목 기준 필터로 좁힌다.
	 */
	@Transactional
	public SyncUpsertResult upsertImportedPrices(List<ImportedMarketPrice> importedPrices) {
		int inserted = 0;
		int updated = 0;
		int skipped = 0;

		for (ImportedMarketPrice imported : importedPrices) {
			if (!isValid(imported)) {
				skipped++;
				continue;
			}
			SecurityItem securityItem = securityItemRepository.findByTicker(imported.ticker()).orElse(null);
			if (securityItem == null) {
				skipped++;
				continue;
			}
			MarketDataSource source = imported.source() == null ? MarketDataSource.KRX : imported.source();
			MarketPrice marketPrice = marketPriceRepository
					.findBySecurityItemIdAndPriceDateAndSource(securityItem.getId(), imported.priceDate(), source)
					.orElse(null);
			if (marketPrice == null) {
				MarketPrice saved = marketPriceRepository.save(new MarketPrice(
						securityItem,
						imported.priceDate(),
						imported.closePrice(),
						imported.currentPrice(),
						imported.changeAmount() == null ? BigDecimal.ZERO : imported.changeAmount(),
						imported.changeRate() == null ? BigDecimal.ZERO : imported.changeRate(),
						imported.volume(),
						source,
						LocalDateTime.now()
				));
				updateHoldingsIfLatest(saved);
				inserted++;
				continue;
			}
			marketPrice.update(
					imported.closePrice(),
					imported.currentPrice(),
					imported.changeAmount() == null ? BigDecimal.ZERO : imported.changeAmount(),
					imported.changeRate() == null ? BigDecimal.ZERO : imported.changeRate(),
					imported.volume(),
					LocalDateTime.now()
			);
			updateHoldingsIfLatest(marketPrice);
			updated++;
		}

		return new SyncUpsertResult(importedPrices.size(), inserted, updated, skipped);
	}

	private boolean isValid(ImportedMarketPrice imported) {
		return imported != null
				&& StringUtils.hasText(imported.ticker())
				&& imported.priceDate() != null
				&& imported.closePrice() != null
				&& imported.currentPrice() != null;
	}

	private void updateHoldingsIfLatest(MarketPrice marketPrice) {
		// TODO: HoldingSnapshot 자동 생성 정책이 정해지면 이 흐름 뒤에 연결한다.
		MarketPrice latest = marketPriceRepository.findBySecurityItemIdOrderByPriceDateDesc(marketPrice.getSecurityItem().getId())
				.stream()
				.max(Comparator.comparing(MarketPrice::getPriceDate).thenComparing(MarketPrice::getFetchedAt))
				.orElse(marketPrice);
		if (!latest.getId().equals(marketPrice.getId())) {
			return;
		}
		holdingRepository.findBySecurityItemId(marketPrice.getSecurityItem().getId())
				.forEach(holding -> holding.updateCurrentPrice(marketPrice.getCurrentPrice()));
	}
}
