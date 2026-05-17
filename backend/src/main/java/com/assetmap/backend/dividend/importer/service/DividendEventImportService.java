package com.assetmap.backend.dividend.importer.service;

import com.assetmap.backend.common.exception.BusinessException;
import com.assetmap.backend.common.exception.ErrorCode;
import com.assetmap.backend.dividend.DataSourceType;
import com.assetmap.backend.dividend.DividendEvent;
import com.assetmap.backend.dividend.DividendEventRepository;
import com.assetmap.backend.dividend.DividendEventType;
import com.assetmap.backend.dividend.DividendPaymentGenerateRequest;
import com.assetmap.backend.dividend.DividendPaymentGenerateResponse;
import com.assetmap.backend.dividend.DividendPaymentService;
import com.assetmap.backend.dividend.importer.dto.DividendImportRequest;
import com.assetmap.backend.dividend.importer.dto.DividendImportResult;
import com.assetmap.backend.dividend.importer.dto.ImportedDividendEvent;
import com.assetmap.backend.dividend.importer.provider.StockDividendProvider;
import com.assetmap.backend.holding.HoldingRepository;
import com.assetmap.backend.securityitem.SecurityItem;
import com.assetmap.backend.securityitem.SecurityItemService;
import com.assetmap.backend.securityitem.SecurityType;
import com.assetmap.backend.transaction.TradeTransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class DividendEventImportService {

	private final StockDividendProvider stockDividendProvider;
	private final HoldingRepository holdingRepository;
	private final TradeTransactionRepository tradeTransactionRepository;
	private final SecurityItemService securityItemService;
	private final DividendEventRepository dividendEventRepository;
	private final DividendPaymentService dividendPaymentService;
	private final int defaultFromYear;

	public DividendEventImportService(
			StockDividendProvider stockDividendProvider,
			HoldingRepository holdingRepository,
			TradeTransactionRepository tradeTransactionRepository,
			SecurityItemService securityItemService,
			DividendEventRepository dividendEventRepository,
			DividendPaymentService dividendPaymentService,
			@Value("${external.public-data.stock-dividend.default-from-year:2020}") int defaultFromYear
	) {
		this.stockDividendProvider = stockDividendProvider;
		this.holdingRepository = holdingRepository;
		this.tradeTransactionRepository = tradeTransactionRepository;
		this.securityItemService = securityItemService;
		this.dividendEventRepository = dividendEventRepository;
		this.dividendPaymentService = dividendPaymentService;
		this.defaultFromYear = defaultFromYear;
	}

	@Transactional
	public DividendImportResult importMySecurities(Long userId, DividendImportRequest request) {
		int fromYear = resolveFromYear(request);
		int toYear = resolveToYear(request);
		validateYearRange(fromYear, toYear);

		Map<Long, SecurityItem> targetSecurities = new LinkedHashMap<>();
		holdingRepository.findDistinctSecurityItemsByUserId(userId).forEach(security -> addTarget(targetSecurities, security));
		tradeTransactionRepository.findDistinctSecurityItemsByUserId(userId).forEach(security -> addTarget(targetSecurities, security));

		DividendImportResult total = DividendImportResult.empty();
		for (SecurityItem securityItem : targetSecurities.values()) {
			total = total.plus(importSecurity(userId, securityItem, fromYear, toYear, false));
		}
		return total;
	}

	@Transactional
	public DividendImportResult importOne(Long userId, DividendImportRequest request) {
		if (request == null || request.securityItemId() == null) {
			throw new BusinessException(ErrorCode.VALIDATION_001);
		}
		int fromYear = resolveFromYear(request);
		int toYear = resolveToYear(request);
		validateYearRange(fromYear, toYear);
		SecurityItem securityItem = securityItemService.getSecurityItem(request.securityItemId());
		return importSecurity(userId, securityItem, fromYear, toYear, true);
	}

	private DividendImportResult importSecurity(Long userId, SecurityItem securityItem, int fromYear, int toYear, boolean countFilteredTarget) {
		if (!isImportTarget(securityItem)) {
			return countFilteredTarget ? new DividendImportResult(0, 0, 1, 0, 0) : DividendImportResult.empty();
		}

		List<ImportedDividendEvent> importedEvents;
		try {
			importedEvents = stockDividendProvider.fetch(securityItem);
		} catch (BusinessException exception) {
			if (exception.getErrorCode() == ErrorCode.COMMON_001) {
				throw exception;
			}
			return new DividendImportResult(1, 0, 0, 0, 1);
		}

		int importedEventCount = 0;
		int skippedEventCount = 0;
		int generatedPaymentCount = 0;
		LocalDate fromDate = LocalDate.of(fromYear, 1, 1);
		LocalDate toDate = LocalDate.of(toYear, 12, 31);

		for (ImportedDividendEvent importedEvent : importedEvents) {
			if (!shouldSave(securityItem, importedEvent, fromDate, toDate)) {
				skippedEventCount++;
				continue;
			}
			if (dividendEventRepository.existsBySecurityItemIdAndRecordDateAndPaymentDateAndDividendPerShareAndSource(
					securityItem.getId(),
					importedEvent.recordDate(),
					importedEvent.paymentDate(),
					importedEvent.dividendPerShare(),
					DataSourceType.PUBLIC_DATA_STOCK_DIVIDEND
			)) {
				skippedEventCount++;
				continue;
			}

			DividendEvent saved = dividendEventRepository.save(new DividendEvent(
					securityItem,
					importedEvent.recordDate().getYear(),
					null,
					null,
					importedEvent.recordDate(),
					importedEvent.paymentDate(),
					DividendEventType.CASH_DIVIDEND,
					importedEvent.dividendPerShare(),
					"KRW",
					DataSourceType.PUBLIC_DATA_STOCK_DIVIDEND
			));
			DividendPaymentGenerateResponse generated = dividendPaymentService.generate(new DividendPaymentGenerateRequest(userId, saved.getId()));
			importedEventCount++;
			generatedPaymentCount += generated.generatedCount();
		}

		return new DividendImportResult(1, importedEventCount, skippedEventCount, generatedPaymentCount, 0);
	}

	private void addTarget(Map<Long, SecurityItem> targetSecurities, SecurityItem securityItem) {
		if (isImportTarget(securityItem)) {
			targetSecurities.putIfAbsent(securityItem.getId(), securityItem);
		}
	}

	private boolean shouldSave(SecurityItem securityItem, ImportedDividendEvent event, LocalDate fromDate, LocalDate toDate) {
		if (event.recordDate() == null || event.recordDate().isBefore(fromDate) || event.recordDate().isAfter(toDate)) {
			return false;
		}
		if (event.dividendPerShare() == null || event.dividendPerShare().compareTo(BigDecimal.ZERO) <= 0) {
			return false;
		}
		if (StringUtils.hasText(event.dividendRecordName()) && normalize(event.dividendRecordName()).contains("무배당")) {
			return false;
		}
		return matchesSecurity(securityItem, event);
	}

	private boolean matchesSecurity(SecurityItem securityItem, ImportedDividendEvent event) {
		String securityName = normalize(securityItem.getName());
		String isinName = normalize(event.isinName());
		String companyName = normalize(event.companyName());
		String stockTypeName = normalize(event.stockTypeName());
		boolean preferredSecurity = securityName.endsWith("우") || securityName.contains("우선");
		boolean preferredEvent = stockTypeName.contains("우선") || isinName.contains("우선");

		if (preferredSecurity) {
			return preferredEvent && (containsNonBlank(isinName, securityName) || containsNonBlank(securityName, companyName));
		}
		if (preferredEvent) {
			return false;
		}
		return containsNonBlank(isinName, securityName) || containsNonBlank(companyName, securityName) || containsNonBlank(securityName, companyName);
	}

	private boolean containsNonBlank(String target, String candidate) {
		return StringUtils.hasText(target) && StringUtils.hasText(candidate) && target.contains(candidate);
	}

	private boolean isImportTarget(SecurityItem securityItem) {
		return securityItem.getSecurityType() == SecurityType.STOCK && isDomesticKrw(securityItem);
	}

	private boolean isDomesticKrw(SecurityItem securityItem) {
		if (!"KRW".equalsIgnoreCase(securityItem.getCurrency())) {
			return false;
		}
		if (!StringUtils.hasText(securityItem.getCountry())) {
			return true;
		}
		String country = normalize(securityItem.getCountry()).toUpperCase(Locale.ROOT);
		return country.equals("KR") || country.equals("KOREA") || country.equals("SOUTHKOREA") || country.equals("대한민국");
	}

	private String normalize(String value) {
		if (value == null) {
			return "";
		}
		return value
				.replace("(주)", "")
				.replace("주식회사", "")
				.replaceAll("[\\s\\-_/().]", "")
				.trim();
	}

	private int resolveFromYear(DividendImportRequest request) {
		return request == null || request.fromYear() == null ? defaultFromYear : request.fromYear();
	}

	private int resolveToYear(DividendImportRequest request) {
		return request == null || request.toYear() == null ? LocalDate.now().getYear() : request.toYear();
	}

	private void validateYearRange(int fromYear, int toYear) {
		if (fromYear > toYear) {
			throw new BusinessException(ErrorCode.VALIDATION_001);
		}
	}
}
