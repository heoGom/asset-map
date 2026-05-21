package com.assetmap.backend.snapshot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.assetmap.backend.account.Account;
import com.assetmap.backend.account.AccountType;
import com.assetmap.backend.holding.Holding;
import com.assetmap.backend.holding.HoldingRepository;
import com.assetmap.backend.securityitem.SecurityItem;
import com.assetmap.backend.securityitem.SecurityType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HoldingSnapshotServiceTest {

	@Mock
	private HoldingRepository holdingRepository;

	@Mock
	private HoldingSnapshotRepository snapshotRepository;

	private HoldingSnapshotService service;

	@BeforeEach
	void setUp() {
		service = new HoldingSnapshotService(holdingRepository, snapshotRepository);
	}

	@Test
	void timelineReturnsCurrentEvaluatedAmountWhenNoSnapshotsExist() {
		Account account = new Account(1L, "테스트", "broker", AccountType.GENERAL, "KRW", null);
		SecurityItem securityItem = new SecurityItem("005930", "삼성전자", "KOSPI", "KOREA", "KRW", SecurityType.STOCK);
		Holding holding = new Holding(1L, account, securityItem, BigDecimal.TEN, BigDecimal.valueOf(50000), BigDecimal.valueOf(70000), "KRW");

		when(snapshotRepository.findByUserIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(eq(1L), any(LocalDate.class), any(LocalDate.class)))
				.thenReturn(List.of());
		when(holdingRepository.findByUserId(1L)).thenReturn(List.of(holding));

		List<AssetTimelineResponse> timeline = service.timeline(1L, null, null);

		assertThat(timeline).hasSize(1);
		assertThat(timeline.get(0).date()).isEqualTo(LocalDate.now());
		assertThat(timeline.get(0).totalAssetAmount()).isEqualByComparingTo("700000.00");
	}
}
