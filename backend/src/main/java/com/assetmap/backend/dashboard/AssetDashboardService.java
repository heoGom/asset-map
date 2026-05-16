package com.assetmap.backend.dashboard;

import com.assetmap.backend.classification.AssetGroup;
import com.assetmap.backend.classification.CountryGroup;
import com.assetmap.backend.classification.Sector;
import com.assetmap.backend.classification.SecurityClassification;
import com.assetmap.backend.classification.SecurityClassificationRepository;
import com.assetmap.backend.classification.StrategyType;
import com.assetmap.backend.holding.Holding;
import com.assetmap.backend.holding.HoldingRepository;
import com.assetmap.backend.holding.MoneyCalculator;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AssetDashboardService {

	private final HoldingRepository holdingRepository;
	private final SecurityClassificationRepository classificationRepository;

	public AssetDashboardService(
			HoldingRepository holdingRepository,
			SecurityClassificationRepository classificationRepository
	) {
		this.holdingRepository = holdingRepository;
		this.classificationRepository = classificationRepository;
	}

	public AssetSummaryResponse summary() {
		List<Holding> holdings = holdingRepository.findAll();
		BigDecimal totalInvested = holdings.stream()
				.map(holding -> MoneyCalculator.amount(holding.getQuantity(), holding.getAveragePrice()))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal totalEvaluated = totalEvaluatedAmount(holdings);
		BigDecimal profitLoss = totalEvaluated.subtract(totalInvested);
		return new AssetSummaryResponse(
				totalInvested,
				totalEvaluated,
				profitLoss,
				MoneyCalculator.rate(profitLoss, totalInvested),
				holdings.size()
		);
	}

	public List<AccountAssetRatioResponse> byAccount() {
		List<Holding> holdings = holdingRepository.findAll();
		BigDecimal total = totalEvaluatedAmount(holdings);
		Map<Long, AccountBucket> buckets = new LinkedHashMap<>();
		for (Holding holding : holdings) {
			BigDecimal amount = evaluatedAmount(holding);
			AccountBucket bucket = buckets.computeIfAbsent(
					holding.getAccount().getId(),
					accountId -> new AccountBucket(accountId, holding.getAccount().getName(), BigDecimal.ZERO)
			);
			bucket.amount = bucket.amount.add(amount);
		}
		return buckets.values().stream()
				.sorted(Comparator.comparing(AccountBucket::accountId))
				.map(bucket -> new AccountAssetRatioResponse(
						bucket.accountId,
						bucket.accountName,
						bucket.amount,
						MoneyCalculator.rate(bucket.amount, total)
				))
				.toList();
	}

	public List<AssetRatioResponse> byCountry() {
		return byClassification(classification -> classification.getCountryGroup(), CountryGroup.UNKNOWN);
	}

	public List<AssetRatioResponse> byType() {
		return byClassification(classification -> classification.getAssetGroup(), AssetGroup.UNKNOWN);
	}

	public List<AssetRatioResponse> bySector() {
		return byClassification(classification -> classification.getSector(), Sector.UNKNOWN);
	}

	public List<AssetRatioResponse> byStrategy() {
		return byClassification(classification -> classification.getStrategyType(), StrategyType.UNKNOWN);
	}

	private <T extends Enum<T>> List<AssetRatioResponse> byClassification(
			Function<SecurityClassification, T> classifier,
			T unknown
	) {
		List<Holding> holdings = holdingRepository.findAll();
		BigDecimal total = totalEvaluatedAmount(holdings);
		Map<String, BigDecimal> amounts = new LinkedHashMap<>();
		for (Holding holding : holdings) {
			String category = classificationRepository.findBySecurityItemId(holding.getSecurityItem().getId())
					.map(classifier)
					.orElse(unknown)
					.name();
			amounts.merge(category, evaluatedAmount(holding), BigDecimal::add);
		}
		return amounts.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.map(entry -> new AssetRatioResponse(entry.getKey(), entry.getValue(), MoneyCalculator.rate(entry.getValue(), total)))
				.toList();
	}

	private BigDecimal totalEvaluatedAmount(List<Holding> holdings) {
		return holdings.stream().map(this::evaluatedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BigDecimal evaluatedAmount(Holding holding) {
		return MoneyCalculator.amount(holding.getQuantity(), holding.getCurrentPrice());
	}

	private static class AccountBucket {
		private final Long accountId;
		private final String accountName;
		private BigDecimal amount;

		private AccountBucket(Long accountId, String accountName, BigDecimal amount) {
			this.accountId = accountId;
			this.accountName = accountName;
			this.amount = amount;
		}

		private Long accountId() {
			return accountId;
		}
	}
}
