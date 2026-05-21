package com.assetmap.backend.dividend;
import com.assetmap.backend.dividend.payment.dto.DividendPaymentResponse;
import com.assetmap.backend.dividend.payment.enums.DividendPaymentStatus;
import com.assetmap.backend.dividend.payment.DividendPaymentService;
import com.assetmap.backend.dividend.event.DividendEventService;
import com.assetmap.backend.dividend.payment.DividendPaymentRepository;
import com.assetmap.backend.dividend.payment.DividendPayment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.assetmap.backend.account.Account;
import com.assetmap.backend.account.AccountRepository;
import com.assetmap.backend.account.AccountService;
import com.assetmap.backend.account.AccountType;
import com.assetmap.backend.securityitem.SecurityItem;
import com.assetmap.backend.securityitem.SecurityItemService;
import com.assetmap.backend.securityitem.SecurityType;
import com.assetmap.backend.transaction.PositionCalculationService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DividendPaymentServiceTest {

	@Mock
	private DividendPaymentRepository paymentRepository;

	@Mock
	private AccountService accountService;

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private SecurityItemService securityItemService;

	@Mock
	private DividendEventService dividendEventService;

	@Mock
	private PositionCalculationService positionCalculationService;

	private DividendPaymentService service;

	@BeforeEach
	void setUp() {
		service = new DividendPaymentService(
				paymentRepository,
				accountService,
				accountRepository,
				securityItemService,
				dividendEventService,
				positionCalculationService
		);
	}

	@Test
	void findByUserIdReturnsPaymentsOrderedByPaymentDate() {
		Account account = new Account(1L, "테스트", "broker", AccountType.GENERAL, "KRW", null);
		ReflectionTestUtils.setField(account, "id", 20L);
		SecurityItem securityItem = new SecurityItem("005930", "삼성전자", "KOSPI", "KOREA", "KRW", SecurityType.STOCK);
		ReflectionTestUtils.setField(securityItem, "id", 10L);
		DividendPayment earlier = payment(1L, account, securityItem, 1L, LocalDate.of(2025, 4, 20));
		DividendPayment later = payment(1L, account, securityItem, 2L, LocalDate.of(2025, 8, 20));

		when(paymentRepository.findByUserIdOrderByPaymentDateAscIdAsc(1L)).thenReturn(List.of(earlier, later));

		List<DividendPaymentResponse> payments = service.findByUserId(1L);

		verify(paymentRepository).findByUserIdOrderByPaymentDateAscIdAsc(1L);
		assertThat(payments).extracting(DividendPaymentResponse::paymentDate)
				.containsExactly(LocalDate.of(2025, 4, 20), LocalDate.of(2025, 8, 20));
	}

	private DividendPayment payment(Long userId, Account account, SecurityItem securityItem, Long id, LocalDate paymentDate) {
		DividendPayment payment = new DividendPayment(
				userId,
				account,
				securityItem,
				null,
				BigDecimal.ONE,
				BigDecimal.valueOf(100),
				BigDecimal.ZERO,
				"KRW",
				BigDecimal.ONE,
				paymentDate,
				DividendPaymentStatus.EXPECTED
		);
		ReflectionTestUtils.setField(payment, "id", id);
		return payment;
	}
}
