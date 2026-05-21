package com.assetmap.backend.dividend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.assetmap.backend.account.Account;
import com.assetmap.backend.account.AccountType;
import com.assetmap.backend.holding.Holding;
import com.assetmap.backend.holding.HoldingRepository;
import com.assetmap.backend.securityitem.SecurityItem;
import com.assetmap.backend.securityitem.SecurityType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DividendDashboardServiceTest {

	@Mock
	private HoldingRepository holdingRepository;

	@Mock
	private DividendEventRepository eventRepository;

	@Mock
	private DividendPaymentRepository paymentRepository;

	private DividendDashboardService service;

	@BeforeEach
	void setUp() {
		service = new DividendDashboardService(holdingRepository, eventRepository, paymentRepository);
	}

	@Test
	void summaryTreatsPastExpectedPaymentsAsReceived() {
		int currentYear = LocalDate.now().getYear();
		Account account = account();
		SecurityItem securityItem = securityItem();
		Holding holding = new Holding(1L, account, securityItem, BigDecimal.TEN, BigDecimal.valueOf(50000), BigDecimal.valueOf(70000), "KRW");
		DividendPayment expectedPastPayment = payment(account, securityItem, LocalDate.of(currentYear, 4, 20), DividendPaymentStatus.EXPECTED, BigDecimal.valueOf(1000));
		DividendPayment paidOldPayment = payment(account, securityItem, LocalDate.of(currentYear - 1, 4, 20), DividendPaymentStatus.PAID, BigDecimal.valueOf(700));

		when(holdingRepository.findByUserId(1L)).thenReturn(List.of(holding));
		when(paymentRepository.findByUserId(1L)).thenReturn(List.of(expectedPastPayment, paidOldPayment));
		when(eventRepository.findBySecurityItemId(10L)).thenReturn(List.of());

		DividendSummaryResponse summary = service.summary(1L);
		List<MonthlyDividendResponse> monthly = service.monthly(1L, currentYear);

		assertThat(summary.currentYearReceivedDividendKrw()).isEqualByComparingTo("1000.00");
		assertThat(summary.totalReceivedDividendKrw()).isEqualByComparingTo("1700.00");
		assertThat(monthly.get(3).amountKrw()).isEqualByComparingTo("1000.00");
	}

	@Test
	void summaryAnnualizesPartialCurrentYearDividendByHistoricalFrequency() {
		int currentYear = LocalDate.now().getYear();
		Account account = account();
		SecurityItem securityItem = securityItem();
		Holding holding = new Holding(1L, account, securityItem, BigDecimal.TEN, BigDecimal.valueOf(50000), BigDecimal.valueOf(70000), "KRW");

		when(holdingRepository.findByUserId(1L)).thenReturn(List.of(holding));
		when(paymentRepository.findByUserId(1L)).thenReturn(List.of());
		when(eventRepository.findBySecurityItemId(10L)).thenReturn(List.of(
				event(securityItem, currentYear - 1, 1, BigDecimal.valueOf(400)),
				event(securityItem, currentYear - 1, 2, BigDecimal.valueOf(400)),
				event(securityItem, currentYear - 1, 3, BigDecimal.valueOf(400)),
				event(securityItem, currentYear - 1, 4, BigDecimal.valueOf(400)),
				event(securityItem, currentYear, 1, BigDecimal.valueOf(500))
		));

		DividendSummaryResponse summary = service.summary(1L);

		assertThat(summary.expectedAnnualDividendKrw()).isEqualByComparingTo("20000.00");
		assertThat(summary.averageMonthlyDividendKrw()).isEqualByComparingTo("1666.67");
	}

	private Account account() {
		Account account = new Account(1L, "테스트", "broker", AccountType.GENERAL, "KRW", null);
		ReflectionTestUtils.setField(account, "id", 20L);
		return account;
	}

	private SecurityItem securityItem() {
		SecurityItem securityItem = new SecurityItem("005930", "삼성전자", "KOSPI", "KOREA", "KRW", SecurityType.STOCK);
		ReflectionTestUtils.setField(securityItem, "id", 10L);
		return securityItem;
	}

	private DividendPayment payment(Account account, SecurityItem securityItem, LocalDate paymentDate, DividendPaymentStatus status, BigDecimal netAmount) {
		return new DividendPayment(1L, account, securityItem, null, BigDecimal.ONE, netAmount, BigDecimal.ZERO, "KRW", BigDecimal.ONE, paymentDate, status);
	}

	private DividendEvent event(SecurityItem securityItem, int year, int quarter, BigDecimal dividendPerShare) {
		YearMonth recordMonth = YearMonth.of(year, quarter * 3);
		LocalDate recordDate = recordMonth.atEndOfMonth();
		return new DividendEvent(securityItem, year, null, null, recordDate, recordDate.plusMonths(1), DividendEventType.CASH_DIVIDEND, dividendPerShare, "KRW", DataSourceType.PUBLIC_DATA_STOCK_DIVIDEND);
	}
}
