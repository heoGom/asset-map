package com.assetmap.backend.snapshot;

import com.assetmap.backend.datasync.admin.dto.DataSyncStatusResponse;
import com.assetmap.backend.datasync.common.DataSyncSource;
import com.assetmap.backend.datasync.common.DataSyncType;
import com.assetmap.backend.datasync.status.DataSyncStatusService;
import com.assetmap.backend.marketprice.MarketDataSource;
import com.assetmap.backend.marketprice.MarketPrice;
import com.assetmap.backend.marketprice.MarketPriceRepository;
import com.assetmap.backend.transaction.TradeTransaction;
import com.assetmap.backend.transaction.TradeTransactionRepository;
import com.assetmap.backend.transaction.TradeType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HoldingSnapshotDateSyncService {

	private static final int PRICE_SCALE = 6;
	private static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.BASIC_ISO_DATE;

	private final HoldingSnapshotRepository snapshotRepository;
	private final TradeTransactionRepository transactionRepository;
	private final MarketPriceRepository marketPriceRepository;
	private final DataSyncStatusService dataSyncStatusService;

	public HoldingSnapshotDateSyncService(
			HoldingSnapshotRepository snapshotRepository,
			TradeTransactionRepository transactionRepository,
			MarketPriceRepository marketPriceRepository,
			DataSyncStatusService dataSyncStatusService
	) {
		this.snapshotRepository = snapshotRepository;
		this.transactionRepository = transactionRepository;
		this.marketPriceRepository = marketPriceRepository;
		this.dataSyncStatusService = dataSyncStatusService;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public HoldingSnapshotDateSyncResult syncDate(LocalDate snapshotDate) {
		String targetKey = targetKey(snapshotDate);
		dataSyncStatusService.markRunning(
				DataSyncType.HOLDING_SNAPSHOT,
				DataSyncSource.INTERNAL,
				targetKey,
				"Holding snapshot sync started. snapshotDate=%s".formatted(snapshotDate)
		);
		try {
			return syncDateInternal(snapshotDate, targetKey);
		} catch (RuntimeException exception) {
			DataSyncStatusResponse status = dataSyncStatusService.markFailed(
					DataSyncType.HOLDING_SNAPSHOT,
					DataSyncSource.INTERNAL,
					targetKey,
					exception.getMessage()
			);
			return new HoldingSnapshotDateSyncResult(snapshotDate, "FAILED", 0, 0, 0, 0, 1, exception.getMessage(), status);
		}
	}

	private HoldingSnapshotDateSyncResult syncDateInternal(LocalDate snapshotDate, String targetKey) {
		Map<Long, MarketPrice> pricesBySecurityId = marketPriceRepository.findByPriceDateAndSource(snapshotDate, MarketDataSource.KRX)
				.stream()
				.collect(Collectors.toMap(price -> price.getSecurityItem().getId(), Function.identity(), (left, right) -> latestFetched(left, right)));
		if (pricesBySecurityId.isEmpty()) {
			String message = "NO_DATA: no KRX market prices for snapshot date. snapshotDate=%s".formatted(snapshotDate);
			DataSyncStatusResponse status = dataSyncStatusService.markNoData(
					DataSyncType.HOLDING_SNAPSHOT,
					DataSyncSource.INTERNAL,
					targetKey,
					snapshotDate,
					message
			);
			return new HoldingSnapshotDateSyncResult(snapshotDate, "NO_DATA", 0, 0, 0, 0, 0, message, status);
		}

		Map<PositionKey, Position> positions = positionsAt(snapshotDate);
		int inserted = 0;
		int updated = 0;
		int skipped = 0;
		int targetPositions = 0;
		for (Position position : positions.values()) {
			if (position.quantity.compareTo(BigDecimal.ZERO) <= 0) {
				skipped++;
				continue;
			}
			targetPositions++;
			MarketPrice price = pricesBySecurityId.get(position.securityItemId());
			if (price == null) {
				skipped++;
				continue;
			}
			BigDecimal currentPrice = price.getClosePrice() == null ? price.getCurrentPrice() : price.getClosePrice();
			BigDecimal evaluatedAmount = position.quantity.multiply(currentPrice).setScale(2, RoundingMode.HALF_UP);
			HoldingSnapshot snapshot = snapshotRepository
					.findByUserIdAndAccountIdAndSecurityItemIdAndSnapshotDate(position.userId(), position.account().getId(), position.securityItem().getId(), snapshotDate)
					.orElse(null);
			if (snapshot == null) {
				snapshotRepository.save(new HoldingSnapshot(
						position.userId(),
						position.account(),
						position.securityItem(),
						position.quantity,
						position.averagePrice,
						currentPrice,
						evaluatedAmount,
						snapshotDate,
						position.currency,
						BigDecimal.ONE,
						evaluatedAmount
				));
				inserted++;
				continue;
			}
			snapshot.updateSnapshot(position.quantity, position.averagePrice, currentPrice, evaluatedAmount, position.currency, BigDecimal.ONE, evaluatedAmount);
			updated++;
		}

		int saved = inserted + updated;
		if (saved == 0) {
			String message = "NO_DATA: no positive positions with KRX market price. snapshotDate=%s targetPositions=%d skipped=%d"
					.formatted(snapshotDate, targetPositions, skipped);
			DataSyncStatusResponse status = dataSyncStatusService.markNoData(
					DataSyncType.HOLDING_SNAPSHOT,
					DataSyncSource.INTERNAL,
					targetKey,
					snapshotDate,
					message
			);
			return new HoldingSnapshotDateSyncResult(snapshotDate, "NO_DATA", targetPositions, 0, 0, skipped, 0, message, status);
		}

		String message = "Holding snapshot sync completed. snapshotDate=%s targetPositions=%d inserted=%d updated=%d skipped=%d"
				.formatted(snapshotDate, targetPositions, inserted, updated, skipped);
		DataSyncStatusResponse status = dataSyncStatusService.markSuccess(
				DataSyncType.HOLDING_SNAPSHOT,
				DataSyncSource.INTERNAL,
				targetKey,
				snapshotDate,
				message
		);
		return new HoldingSnapshotDateSyncResult(snapshotDate, "SUCCESS", targetPositions, inserted, updated, skipped, 0, message, status);
	}

	private Map<PositionKey, Position> positionsAt(LocalDate snapshotDate) {
		Map<PositionKey, Position> positions = new LinkedHashMap<>();
		for (TradeTransaction transaction : transactionRepository.findAllByTradeDateLessThanEqualForSnapshot(snapshotDate)) {
			PositionKey key = new PositionKey(transaction.getUserId(), transaction.getAccount().getId(), transaction.getSecurityItem().getId());
			Position position = positions.computeIfAbsent(key, ignored -> new Position(transaction));
			position.apply(transaction);
		}
		return positions;
	}

	private MarketPrice latestFetched(MarketPrice left, MarketPrice right) {
		if (left.getFetchedAt() == null) {
			return right;
		}
		if (right.getFetchedAt() == null) {
			return left;
		}
		return right.getFetchedAt().isAfter(left.getFetchedAt()) ? right : left;
	}

	public static String targetKey(LocalDate snapshotDate) {
		return "HOLDING_SNAPSHOT_" + snapshotDate.format(COMPACT_DATE);
	}

	private record PositionKey(Long userId, Long accountId, Long securityItemId) {
	}

	private static final class Position {
		private final Long userId;
		private final com.assetmap.backend.account.Account account;
		private final com.assetmap.backend.securityitem.SecurityItem securityItem;
		private BigDecimal quantity = BigDecimal.ZERO;
		private BigDecimal averagePrice = BigDecimal.ZERO.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
		private String currency;

		private Position(TradeTransaction transaction) {
			this.userId = transaction.getUserId();
			this.account = transaction.getAccount();
			this.securityItem = transaction.getSecurityItem();
			this.currency = transaction.getCurrency();
		}

		private void apply(TradeTransaction transaction) {
			currency = transaction.getCurrency();
			if (transaction.getTradeType() == TradeType.SELL) {
				quantity = quantity.subtract(transaction.getQuantity());
				if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
					quantity = BigDecimal.ZERO;
					averagePrice = BigDecimal.ZERO.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
				}
				return;
			}
			BigDecimal oldCost = quantity.multiply(averagePrice);
			BigDecimal newCost = transaction.getQuantity().multiply(transaction.getPrice());
			BigDecimal newQuantity = quantity.add(transaction.getQuantity());
			averagePrice = newQuantity.compareTo(BigDecimal.ZERO) == 0
					? BigDecimal.ZERO.setScale(PRICE_SCALE, RoundingMode.HALF_UP)
					: oldCost.add(newCost).divide(newQuantity, PRICE_SCALE, RoundingMode.HALF_UP);
			quantity = newQuantity;
		}

		private Long userId() { return userId; }
		private com.assetmap.backend.account.Account account() { return account; }
		private com.assetmap.backend.securityitem.SecurityItem securityItem() { return securityItem; }
		private Long securityItemId() { return securityItem.getId(); }
	}
}
