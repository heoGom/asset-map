package com.assetmap.backend.dividend.dashboard;
import com.assetmap.backend.dividend.dashboard.dto.YearlyDividendResponse;
import com.assetmap.backend.dividend.dashboard.dto.SecurityDividendResponse;
import com.assetmap.backend.dividend.dashboard.dto.MonthlyDividendResponse;
import com.assetmap.backend.dividend.dashboard.dto.DividendGrowthResponse;
import com.assetmap.backend.dividend.dashboard.dto.DividendSummaryResponse;
import com.assetmap.backend.dividend.payment.enums.DividendPaymentStatus;
import com.assetmap.backend.dividend.payment.DividendPaymentRepository;
import com.assetmap.backend.dividend.event.DividendEventRepository;
import com.assetmap.backend.dividend.payment.DividendPayment;
import com.assetmap.backend.dividend.event.DividendEvent;

import com.assetmap.backend.holding.Holding;
import com.assetmap.backend.holding.HoldingRepository;
import com.assetmap.backend.holding.MoneyCalculator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DividendDashboardService {

	private final HoldingRepository holdingRepository;
	private final DividendEventRepository eventRepository;
	private final DividendPaymentRepository paymentRepository;

	public DividendDashboardService(HoldingRepository holdingRepository, DividendEventRepository eventRepository, DividendPaymentRepository paymentRepository) {
		this.holdingRepository = holdingRepository;
		this.eventRepository = eventRepository;
		this.paymentRepository = paymentRepository;
	}

	public DividendSummaryResponse summary(Long userId) {
		int currentYear = LocalDate.now().getYear();
		List<Holding> holdings = holdingRepository.findByUserId(userId);
		List<DividendPayment> receivedPayments = receivedPayments(userId);
		BigDecimal expectedAnnual = expectedAnnualDividend(userId, currentYear);
		BigDecimal invested = holdings.stream()
				.map(holding -> MoneyCalculator.amount(holding.getQuantity(), holding.getAveragePrice()))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal evaluated = holdings.stream()
				.map(holding -> MoneyCalculator.amount(holding.getQuantity(), holding.getCurrentPrice()))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal currentYearReceived = receivedPayments.stream()
				.filter(payment -> payment.getPaymentDate() != null && payment.getPaymentDate().getYear() == currentYear)
				.map(DividendPayment::getNetAmountKrw)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal totalReceived = receivedPayments.stream()
				.map(DividendPayment::getNetAmountKrw)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		return new DividendSummaryResponse(
				expectedAnnual,
				expectedAnnual.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP),
				MoneyCalculator.rate(expectedAnnual, evaluated),
				MoneyCalculator.rate(expectedAnnual, invested),
				currentYearReceived,
				totalReceived
		);
	}

	public List<MonthlyDividendResponse> monthly(Long userId, int year) {
		Map<Integer, BigDecimal> amounts = new LinkedHashMap<>();
		for (int month = 1; month <= 12; month++) {
			amounts.put(month, BigDecimal.ZERO);
		}
		for (DividendPayment payment : receivedPayments(userId)) {
			if (payment.getPaymentDate() != null && payment.getPaymentDate().getYear() == year) {
				int month = payment.getPaymentDate().getMonthValue();
				amounts.merge(month, payment.getNetAmountKrw(), BigDecimal::add);
			}
		}
		return amounts.entrySet().stream()
				.map(entry -> new MonthlyDividendResponse(entry.getKey(), entry.getValue()))
				.toList();
	}

	public List<YearlyDividendResponse> yearly(Long userId) {
		Map<Integer, BigDecimal> amounts = new LinkedHashMap<>();
		for (DividendPayment payment : receivedPayments(userId)) {
			if (payment.getPaymentDate() != null) {
				amounts.merge(payment.getPaymentDate().getYear(), payment.getNetAmountKrw(), BigDecimal::add);
			}
		}
		return amounts.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.map(entry -> new YearlyDividendResponse(entry.getKey(), entry.getValue()))
				.toList();
	}

	public List<SecurityDividendResponse> bySecurity(Long userId) {
		int currentYear = LocalDate.now().getYear();
		List<Holding> holdings = holdingRepository.findByUserId(userId);
		List<DividendPayment> receivedPayments = receivedPayments(userId);
		BigDecimal totalExpected = expectedAnnualDividend(userId, currentYear);
		return holdings.stream()
				.map(holding -> {
					BigDecimal annualPerShare = estimatedAnnualDividendPerShare(holding.getSecurityItem().getId(), currentYear);
					BigDecimal expected = holding.getQuantity().multiply(annualPerShare).setScale(2, RoundingMode.HALF_UP);
					BigDecimal received = receivedPayments.stream()
							.filter(payment -> payment.getSecurityItem().getId().equals(holding.getSecurityItem().getId()))
							.map(DividendPayment::getNetAmountKrw)
							.reduce(BigDecimal.ZERO, BigDecimal::add);
					return new SecurityDividendResponse(
							holding.getSecurityItem().getId(),
							holding.getSecurityItem().getTicker(),
							holding.getSecurityItem().getName(),
							expected,
							received,
							MoneyCalculator.rate(annualPerShare, holding.getCurrentPrice()),
							MoneyCalculator.rate(annualPerShare, holding.getAveragePrice()),
							MoneyCalculator.rate(expected, totalExpected)
					);
				})
				.toList();
	}

	public List<DividendGrowthResponse> growth(Long securityItemId) {
		Map<Integer, BigDecimal> yearly = new LinkedHashMap<>();
		for (DividendEvent event : eventRepository.findBySecurityItemId(securityItemId)) {
			yearly.merge(event.getDividendYear(), event.getDividendPerShare(), BigDecimal::add);
		}
		List<DividendGrowthResponse> responses = new ArrayList<>();
		BigDecimal previous = BigDecimal.ZERO;
		for (Map.Entry<Integer, BigDecimal> entry : yearly.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
			BigDecimal growthRate = previous.compareTo(BigDecimal.ZERO) == 0
					? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
					: MoneyCalculator.rate(entry.getValue().subtract(previous), previous);
			responses.add(new DividendGrowthResponse(entry.getKey(), entry.getValue(), growthRate));
			previous = entry.getValue();
		}
		return responses.stream().sorted(Comparator.comparing(DividendGrowthResponse::year)).toList();
	}

	private BigDecimal expectedAnnualDividend(Long userId, int year) {
		return holdingRepository.findByUserId(userId).stream()
				.map(holding -> holding.getQuantity().multiply(estimatedAnnualDividendPerShare(holding.getSecurityItem().getId(), year)))
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal estimatedAnnualDividendPerShare(Long securityItemId, int year) {
		List<DividendEvent> events = eventRepository.findBySecurityItemId(securityItemId).stream()
				.filter(event -> event.getDividendYear() != null && event.getDividendYear() <= year)
				.toList();
		if (events.isEmpty()) {
			return BigDecimal.ZERO;
		}

		List<DividendEvent> targetYearEvents = events.stream()
				.filter(event -> event.getDividendYear() == year)
				.toList();
		BigDecimal targetYearSum = targetYearEvents.stream()
				.map(DividendEvent::getDividendPerShare)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		int inferredFrequency = inferDividendFrequency(events, year);
		if (targetYearSum.compareTo(BigDecimal.ZERO) > 0 && targetYearEvents.size() >= inferredFrequency) {
			return targetYearSum;
		}

		BigDecimal latestPerShare = events.stream()
				.filter(event -> event.getRecordDate() != null && !event.getRecordDate().isAfter(LocalDate.of(year, 12, 31)))
				.max(Comparator.comparing(DividendEvent::getRecordDate))
				.map(DividendEvent::getDividendPerShare)
				.orElse(BigDecimal.ZERO);
		BigDecimal annualized = latestPerShare.multiply(BigDecimal.valueOf(inferredFrequency));
		if (targetYearSum.compareTo(BigDecimal.ZERO) > 0) {
			return annualized.max(targetYearSum);
		}
		return annualized;
	}

	private int inferDividendFrequency(List<DividendEvent> events, int year) {
		Map<Integer, Long> countByYear = events.stream()
				.filter(event -> event.getDividendYear() != null && event.getDividendYear() < year)
				.collect(Collectors.groupingBy(DividendEvent::getDividendYear, Collectors.counting()));
		long maxHistoricalCount = countByYear.values().stream()
				.mapToLong(Long::longValue)
				.max()
				.orElse(0);
		if (maxHistoricalCount > 0) {
			return Math.max(1, Math.toIntExact(maxHistoricalCount));
		}
		long currentCount = events.stream()
				.filter(event -> event.getDividendYear() != null && event.getDividendYear() == year)
				.count();
		return Math.max(1, Math.toIntExact(currentCount));
	}

	private List<DividendPayment> receivedPayments(Long userId) {
		LocalDate today = LocalDate.now();
		return paymentRepository.findByUserId(userId).stream()
				.filter(payment -> payment.getStatus() == DividendPaymentStatus.PAID
						|| (payment.getPaymentDate() != null && !payment.getPaymentDate().isAfter(today)))
				.toList();
	}
}
