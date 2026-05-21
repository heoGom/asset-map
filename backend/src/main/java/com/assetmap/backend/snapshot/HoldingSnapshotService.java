package com.assetmap.backend.snapshot;

import com.assetmap.backend.holding.Holding;
import com.assetmap.backend.holding.HoldingRepository;
import com.assetmap.backend.holding.MoneyCalculator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HoldingSnapshotService {

	private final HoldingRepository holdingRepository;
	private final HoldingSnapshotRepository snapshotRepository;

	public HoldingSnapshotService(HoldingRepository holdingRepository, HoldingSnapshotRepository snapshotRepository) {
		this.holdingRepository = holdingRepository;
		this.snapshotRepository = snapshotRepository;
	}

	@Transactional
	public SnapshotSaveResponse saveCurrentHoldings(SnapshotSaveRequest request) {
		LocalDate snapshotDate = request.snapshotDate() == null ? LocalDate.now() : request.snapshotDate();
		BigDecimal exchangeRate = BigDecimal.ONE;
		List<HoldingSnapshot> snapshots = holdingRepository.findByUserId(request.userId()).stream()
				.map(holding -> {
					BigDecimal evaluatedAmount = MoneyCalculator.amount(holding.getQuantity(), holding.getCurrentPrice());
					return new HoldingSnapshot(
							holding.getUserId(),
							holding.getAccount(),
							holding.getSecurityItem(),
							holding.getQuantity(),
							holding.getAveragePrice(),
							holding.getCurrentPrice(),
							evaluatedAmount,
							snapshotDate,
							holding.getCurrency(),
							exchangeRate,
							evaluatedAmount.multiply(exchangeRate)
					);
				})
				.toList();
		snapshotRepository.saveAll(snapshots);
		return new SnapshotSaveResponse(snapshotDate, snapshots.size());
	}

	public List<AssetTimelineResponse> timeline(Long userId, LocalDate from, LocalDate to) {
		Map<LocalDate, BigDecimal> amounts = new LinkedHashMap<>();
		for (HoldingSnapshot snapshot : snapshots(userId, from, to)) {
			amounts.merge(snapshot.getSnapshotDate(), snapshot.getEvaluatedAmountKrw(), BigDecimal::add);
		}
		if (amounts.isEmpty()) {
			BigDecimal currentEvaluatedAmount = holdingRepository.findByUserId(userId).stream()
					.map(holding -> MoneyCalculator.amount(holding.getQuantity(), holding.getCurrentPrice()))
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			if (currentEvaluatedAmount.compareTo(BigDecimal.ZERO) > 0) {
				amounts.put(LocalDate.now(), currentEvaluatedAmount);
			}
		}
		return amounts.entrySet().stream()
				.map(entry -> new AssetTimelineResponse(entry.getKey(), entry.getValue()))
				.toList();
	}

	public List<AccountTimelineResponse> byAccount(Long userId, LocalDate from, LocalDate to) {
		Map<AccountSnapshotKey, BigDecimal> amounts = new LinkedHashMap<>();
		for (HoldingSnapshot snapshot : snapshots(userId, from, to)) {
			AccountSnapshotKey key = new AccountSnapshotKey(
					snapshot.getSnapshotDate(),
					snapshot.getAccount().getId(),
					snapshot.getAccount().getName()
			);
			amounts.merge(key, snapshot.getEvaluatedAmountKrw(), BigDecimal::add);
		}
		return amounts.entrySet().stream()
				.sorted(Comparator.comparing((Map.Entry<AccountSnapshotKey, BigDecimal> entry) -> entry.getKey().date)
						.thenComparing(entry -> entry.getKey().accountId))
				.map(entry -> new AccountTimelineResponse(
						entry.getKey().date,
						entry.getKey().accountId,
						entry.getKey().accountName,
						entry.getValue()
				))
				.toList();
	}

	private List<HoldingSnapshot> snapshots(Long userId, LocalDate from, LocalDate to) {
		LocalDate start = from == null ? LocalDate.of(1900, 1, 1) : from;
		LocalDate end = to == null ? LocalDate.of(9999, 12, 31) : to;
		return snapshotRepository.findByUserIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(userId, start, end);
	}

	private record AccountSnapshotKey(LocalDate date, Long accountId, String accountName) {
	}
}
