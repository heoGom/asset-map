package com.assetmap.backend.dividend.importer.service;

import com.assetmap.backend.common.exception.BusinessException;
import com.assetmap.backend.common.exception.ErrorCode;
import com.assetmap.backend.dividend.DataSourceType;
import com.assetmap.backend.dividend.DividendEventRepository;
import com.assetmap.backend.dividend.importer.dto.DividendImportRequest;
import com.assetmap.backend.dividend.importer.dto.DividendImportResult;
import com.assetmap.backend.dividend.importer.dto.DividendImportSkipReason;
import com.assetmap.backend.dividend.importer.dto.DividendSecurityImportResult;
import com.assetmap.backend.dividend.importer.dto.DividendSkipSummary;
import com.assetmap.backend.dividend.importer.dto.ImportedDividendEvent;
import com.assetmap.backend.dividend.importer.dto.StockDividendFetchResult;
import com.assetmap.backend.dividend.importer.service.DividendEventImportOperationService.ImportedDividendOperationResult;
import com.assetmap.backend.dividend.importer.provider.StockDividendProvider;
import com.assetmap.backend.securityitem.SecurityItem;
import com.assetmap.backend.securityitem.SecurityItemService;
import com.assetmap.backend.securityitem.SecurityType;
import com.assetmap.backend.transaction.TradeTransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class DividendEventImportService {

	private static final Logger log = LoggerFactory.getLogger(DividendEventImportService.class);
	private static final Pattern PREFERRED_SUFFIX = Pattern.compile("(.+?)([2-9]?우B?|우선주?)$");

	private final StockDividendProvider stockDividendProvider;
	private final DividendSearchTermResolver searchTermResolver;
	private final TradeTransactionRepository tradeTransactionRepository;
	private final SecurityItemService securityItemService;
	private final DividendEventRepository dividendEventRepository;
	private final DividendEventImportOperationService importOperationService;
	private final int defaultFromYear;

	public DividendEventImportService(
			StockDividendProvider stockDividendProvider,
			DividendSearchTermResolver searchTermResolver,
			TradeTransactionRepository tradeTransactionRepository,
			SecurityItemService securityItemService,
			DividendEventRepository dividendEventRepository,
			DividendEventImportOperationService importOperationService,
			@Value("${external.public-data.stock-dividend.default-from-year:2020}") int defaultFromYear
	) {
		this.stockDividendProvider = stockDividendProvider;
		this.searchTermResolver = searchTermResolver;
		this.tradeTransactionRepository = tradeTransactionRepository;
		this.securityItemService = securityItemService;
		this.dividendEventRepository = dividendEventRepository;
		this.importOperationService = importOperationService;
		this.defaultFromYear = defaultFromYear;
	}

	@Transactional
	public DividendImportResult importMySecurities(Long userId, DividendImportRequest request) {
		int fromYear = resolveFromYear(request);
		int toYear = resolveToYear(request);
		validateYearRange(fromYear, toYear);

		Map<Long, SecurityItem> targetSecurities = new LinkedHashMap<>();
		tradeTransactionRepository.findDistinctSecurityItemsByUserIdAndSecurityTypes(userId, List.of(SecurityType.STOCK))
				.forEach(security -> addTarget(targetSecurities, security));
		log.info("Stock dividend import targets resolved. userId={} fromYear={} toYear={} targetSecurityCount={}",
				userId, fromYear, toYear, targetSecurities.size());

		DividendImportResult total = DividendImportResult.empty();
		for (SecurityItem securityItem : targetSecurities.values()) {
			total = total.plus(importSecurity(List.of(userId), securityItem, fromYear, toYear, false));
		}
		return total;
	}

	@Transactional
	public DividendImportResult importTradedStockSecurities(DividendImportRequest request) {
		int fromYear = resolveFromYear(request);
		int toYear = resolveToYear(request);
		validateYearRange(fromYear, toYear);

		List<SecurityItem> targetSecurities = tradeTransactionRepository.findDistinctSecurityItemsBySecurityTypes(List.of(SecurityType.STOCK))
				.stream()
				.filter(this::isImportTarget)
				.toList();
		log.info("Stock dividend import targets resolved for all traded securities. fromYear={} toYear={} targetSecurityCount={}",
				fromYear, toYear, targetSecurities.size());

		DividendImportResult total = DividendImportResult.empty();
		for (SecurityItem securityItem : targetSecurities) {
			List<Long> userIds = tradeTransactionRepository.findDistinctUserIdsBySecurityItemId(securityItem.getId());
			total = total.plus(importSecurity(userIds, securityItem, fromYear, toYear, false));
		}
		return total;
	}

	@Transactional
	public DividendImportResult importTradedStockSecurity(SecurityItem securityItem, DividendImportRequest request) {
		int fromYear = resolveFromYear(request);
		int toYear = resolveToYear(request);
		validateYearRange(fromYear, toYear);
		List<Long> userIds = tradeTransactionRepository.findDistinctUserIdsBySecurityItemId(securityItem.getId());
		try {
			return importSecurity(userIds, securityItem, fromYear, toYear, false);
		} catch (BusinessException exception) {
			DividendSecurityImportResult securityResult = securityResult(
					securityItem,
					searchTermResolver.resolve(securityItem),
					null,
					"",
					"",
					0,
					0,
					0,
					0,
					0,
					"FAILED",
					exception.getErrorCode().name(),
					Map.of()
			);
			return result(1, 0, 0, 0, 1, securityResult);
		} catch (RuntimeException exception) {
			DividendSecurityImportResult securityResult = securityResult(
					securityItem,
					searchTermResolver.resolve(securityItem),
					null,
					"",
					"",
					0,
					0,
					0,
					0,
					0,
					"FAILED",
					exception.getMessage(),
					Map.of()
			);
			return result(1, 0, 0, 0, 1, securityResult);
		}
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
		return importSecurity(List.of(userId), securityItem, fromYear, toYear, true);
	}

	private DividendImportResult importSecurity(List<Long> userIds, SecurityItem securityItem, int fromYear, int toYear, boolean countFilteredTarget) {
		List<String> searchTerms = searchTermResolver.resolve(securityItem);
		if (!isImportTarget(securityItem)) {
			log.info("Stock dividend import skipped target. securityId={} name={} type={} currency={} country={} reason=WRONG_STOCK_TYPE",
					securityItem.getId(), securityItem.getName(), securityItem.getSecurityType(), securityItem.getCurrency(), securityItem.getCountry());
			DividendSecurityImportResult securityResult = securityResult(
					securityItem,
					searchTerms,
					null,
					"",
					"",
					0,
					0,
					0,
					countFilteredTarget ? 1 : 0,
					0,
					"SKIPPED",
					"STOCK/KRW 국내 종목만 import 대상입니다.",
					skipCounts(DividendImportSkipReason.WRONG_STOCK_TYPE)
			);
			return result(countFilteredTarget ? 0 : 0, 0, countFilteredTarget ? 1 : 0, 0, 0, securityResult);
		}

		log.info("Stock dividend import started. userId={} securityId={} name={} ticker={} fromYear={} toYear={} searchTerms={}",
				userIds, securityItem.getId(), securityItem.getName(), securityItem.getTicker(), fromYear, toYear, searchTerms);

		List<StockDividendFetchResult> fetchResults = new ArrayList<>();
		for (String searchTerm : searchTerms) {
			try {
				fetchResults.add(stockDividendProvider.fetch(searchTerm));
			} catch (BusinessException exception) {
				if (exception.getErrorCode() == ErrorCode.CONFIG_ERROR || exception.getErrorCode() == ErrorCode.API_AUTH_ERROR) {
					throw exception;
				}
				log.warn("Stock dividend import failed at provider stage. securityId={} name={} searchTerm={} errorCode={} message={}",
						securityItem.getId(), securityItem.getName(), searchTerm, exception.getErrorCode(), exception.getMessage());
				DividendSecurityImportResult securityResult = securityResult(
						securityItem,
						searchTerms,
						null,
						"",
						"",
						0,
						0,
						0,
						0,
						0,
						"FAILED",
						exception.getErrorCode().name(),
						Map.of()
				);
				return result(1, 0, 0, 0, 1, securityResult);
			}
		}

		StockDividendFetchResult failedResponse = fetchResults.stream()
				.filter(result -> StringUtils.hasText(result.resultCode()))
				.filter(result -> !"00".equals(result.resultCode()))
				.findFirst()
				.orElse(null);
		if (failedResponse != null) {
			log.warn("Stock dividend import failed at API response stage. securityId={} name={} resultCode={} resultMsg={}",
					securityItem.getId(), securityItem.getName(), failedResponse.resultCode(), failedResponse.resultMsg());
			DividendSecurityImportResult securityResult = securityResult(
					securityItem,
					searchTerms,
					failedResponse.httpStatus() == 0 ? null : failedResponse.httpStatus(),
					failedResponse.resultCode(),
					failedResponse.resultMsg(),
					failedResponse.totalCount(),
					failedResponse.itemCount(),
					0,
					0,
					0,
					"FAILED",
					ErrorCode.API_RESPONSE_ERROR.name(),
					Map.of()
			);
			return result(1, 0, 0, 0, 1, securityResult);
		}

		ImportAccumulator accumulator = new ImportAccumulator(fetchResults);
		LocalDate fromDate = LocalDate.of(fromYear, 1, 1);
		LocalDate toDate = LocalDate.of(toYear, 12, 31);
		List<ImportedDividendEvent> importedEvents = fetchResults.stream().flatMap(result -> result.items().stream()).toList();

		if (importedEvents.isEmpty()) {
			accumulator.skip(DividendImportSkipReason.NO_DATA);
			return result(1, 0, accumulator.skippedCount, 0, 0, accumulator.toSecurityResult(securityItem, searchTerms, "SKIPPED", "조회된 배당 데이터가 없습니다."));
		}

		for (ImportedDividendEvent importedEvent : importedEvents) {
			DividendImportSkipReason skipReason = skipReason(securityItem, importedEvent, fromDate, toDate);
			if (skipReason != null) {
				accumulator.skip(skipReason);
				logSkip(securityItem, importedEvent, skipReason);
				continue;
			}
			var existingEvent = dividendEventRepository.findFirstBySecurityItemIdAndRecordDateAndDividendPerShareAndSource(
					securityItem.getId(),
					importedEvent.recordDate(),
					importedEvent.dividendPerShare(),
					DataSourceType.PUBLIC_DATA_STOCK_DIVIDEND
			);
			if (existingEvent.isPresent()) {
				try {
					ImportedDividendOperationResult operationResult = importOperationService.generatePayments(userIds, existingEvent.get().getId());
					accumulator.generatedPaymentCount += operationResult.generatedPaymentCount();
				} catch (DividendEventImportOperationException exception) {
					log.warn("Stock dividend duplicate payment generation failed. securityId={} name={} reason={} recordDate={} dividendPerShare={} error={}",
							securityItem.getId(), securityItem.getName(), exception.getReason(), importedEvent.recordDate(), importedEvent.dividendPerShare(), exception.getCause().toString());
				}
				accumulator.skip(DividendImportSkipReason.DUPLICATE_EVENT);
				logSkip(securityItem, importedEvent, DividendImportSkipReason.DUPLICATE_EVENT);
				continue;
			}

			try {
				ImportedDividendOperationResult operationResult = importOperationService.saveEventAndGeneratePayments(userIds, securityItem, importedEvent);
				accumulator.importedCount++;
				accumulator.generatedPaymentCount += operationResult.generatedPaymentCount();
				log.info("Stock dividend import item saved and payment generated. securityId={} name={} eventId={} recordDate={} paymentDate={} dividendPerShare={} generatedCount={}",
						securityItem.getId(), securityItem.getName(), operationResult.eventId(), importedEvent.recordDate(), importedEvent.paymentDate(), importedEvent.dividendPerShare(), operationResult.generatedPaymentCount());
			} catch (DividendEventImportOperationException exception) {
				accumulator.skip(exception.getReason());
				log.warn("Stock dividend import operation failed. securityId={} name={} reason={} recordDate={} dividendPerShare={} error={}",
						securityItem.getId(), securityItem.getName(), exception.getReason(), importedEvent.recordDate(), importedEvent.dividendPerShare(), exception.getCause().toString());
			}
		}

		String status = accumulator.importedCount > 0 ? "SUCCESS" : "SKIPPED";
		String message = accumulator.importedCount > 0 ? "배당 이벤트를 저장하고 내 배당금을 생성했습니다." : "저장된 배당 이벤트가 없습니다.";
		log.info("Stock dividend import finished. securityId={} name={} importedEventCount={} skippedEventCount={} generatedPaymentCount={}",
				securityItem.getId(), securityItem.getName(), accumulator.importedCount, accumulator.skippedCount, accumulator.generatedPaymentCount);
		return result(1, accumulator.importedCount, accumulator.skippedCount, accumulator.generatedPaymentCount, 0,
				accumulator.toSecurityResult(securityItem, searchTerms, status, message));
	}

	private void addTarget(Map<Long, SecurityItem> targetSecurities, SecurityItem securityItem) {
		if (isImportTarget(securityItem)) {
			targetSecurities.putIfAbsent(securityItem.getId(), securityItem);
		}
	}

	private DividendImportSkipReason skipReason(SecurityItem securityItem, ImportedDividendEvent event, LocalDate fromDate, LocalDate toDate) {
		if (event.recordDate() == null) {
			return DividendImportSkipReason.NO_RECORD_DATE;
		}
		if (event.recordDate().isBefore(fromDate) || event.recordDate().isAfter(toDate)) {
			return DividendImportSkipReason.BEFORE_FROM_YEAR;
		}
		if (StringUtils.hasText(event.dividendRecordName()) && normalize(event.dividendRecordName()).contains("무배당")) {
			return DividendImportSkipReason.ZERO_DIVIDEND;
		}
		if (event.dividendPerShare() == null || event.dividendPerShare().compareTo(BigDecimal.ZERO) <= 0) {
			return DividendImportSkipReason.ZERO_DIVIDEND;
		}
		return matchDecision(securityItem, event).skipReason();
	}

	private MatchDecision matchDecision(SecurityItem securityItem, ImportedDividendEvent event) {
		StockClass securityClass = stockClassFromSecurityName(securityItem.getName());
		StockClass eventClass = stockClassFromEvent(event);
		if (eventClass.kind() == StockKind.UNKNOWN) {
			return MatchDecision.skip(DividendImportSkipReason.AMBIGUOUS_MATCH);
		}
		if (securityClass.kind() == StockKind.COMMON && eventClass.kind() != StockKind.COMMON) {
			return MatchDecision.skip(DividendImportSkipReason.WRONG_STOCK_TYPE);
		}
		if (securityClass.kind() == StockKind.PREFERRED) {
			if (eventClass.kind() == StockKind.COMMON) {
				return MatchDecision.skip(DividendImportSkipReason.WRONG_STOCK_TYPE);
			}
			if (securityClass.rank() > 1 && eventClass.rank() == 1) {
				return MatchDecision.skip(DividendImportSkipReason.AMBIGUOUS_MATCH);
			}
			if (securityClass.rank() != eventClass.rank()) {
				return MatchDecision.skip(DividendImportSkipReason.WRONG_STOCK_TYPE);
			}
		}
		return companyMatches(securityItem, event) ? MatchDecision.match() : MatchDecision.skip(DividendImportSkipReason.MATCH_FAILED);
	}

	private boolean companyMatches(SecurityItem securityItem, ImportedDividendEvent event) {
		String expectedCompany = normalize(expectedCompanyName(securityItem));
		String companyName = normalize(event.companyName());
		String isinName = normalize(event.isinName());
		return containsNonBlank(companyName, expectedCompany)
				|| containsNonBlank(isinName, expectedCompany)
				|| containsNonBlank(expectedCompany, companyName);
	}

	private String expectedCompanyName(SecurityItem securityItem) {
		String name = securityItem.getName();
		String preferredBase = searchTermResolver.preferredBaseName(name);
		if (StringUtils.hasText(preferredBase)) {
			return mapCompanyName(preferredBase);
		}
		return mapCompanyName(name);
	}

	private String mapCompanyName(String name) {
		return switch (name) {
			case "현대차", "현대차우", "현대차2우B", "현대차3우B" -> "현대자동차";
			default -> name;
		};
	}

	private StockClass stockClassFromSecurityName(String securityName) {
		String normalized = normalize(securityName);
		Matcher matcher = PREFERRED_SUFFIX.matcher(normalized);
		if (!matcher.matches()) {
			return new StockClass(StockKind.COMMON, 0);
		}
		return new StockClass(StockKind.PREFERRED, preferredRank(matcher.group(2)));
	}

	private StockClass stockClassFromEvent(ImportedDividendEvent event) {
		String stockTypeName = normalize(event.stockTypeName());
		String isinName = normalize(event.isinName());
		if (stockTypeName.contains("보통주")) {
			return new StockClass(StockKind.COMMON, 0);
		}
		if (stockTypeName.contains("우선주")) {
			return new StockClass(StockKind.PREFERRED, preferredRank(stockTypeName));
		}
		if (isinName.matches(".*[1-9]우.*") || isinName.contains("우선")) {
			return new StockClass(StockKind.PREFERRED, preferredRank(isinName));
		}
		return new StockClass(StockKind.UNKNOWN, 0);
	}

	private int preferredRank(String value) {
		String normalized = normalize(value);
		for (int rank = 2; rank <= 9; rank++) {
			if (normalized.contains(rank + "우")) {
				return rank;
			}
		}
		return 1;
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

	private DividendImportResult result(
			int targetSecurityCount,
			int importedEventCount,
			int skippedEventCount,
			int generatedPaymentCount,
			int failedSecurityCount,
			DividendSecurityImportResult securityResult
	) {
		return new DividendImportResult(targetSecurityCount, importedEventCount, skippedEventCount, generatedPaymentCount, failedSecurityCount, List.of(securityResult));
	}

	private DividendSecurityImportResult securityResult(
			SecurityItem securityItem,
			List<String> searchTerms,
			Integer httpStatus,
			String resultCode,
			String resultMsg,
			int totalCount,
			int itemCount,
			int importedCount,
			int skippedCount,
			int generatedPaymentCount,
			String status,
			String message,
			Map<DividendImportSkipReason, Integer> skipCounts
	) {
		return new DividendSecurityImportResult(
				securityItem.getId(),
				securityItem.getName(),
				searchTerms,
				httpStatus,
				resultCode,
				resultMsg,
				totalCount,
				itemCount,
				importedCount,
				skippedCount,
				generatedPaymentCount,
				status,
				message,
				toSkipSummaries(skipCounts)
		);
	}

	private List<DividendSkipSummary> toSkipSummaries(Map<DividendImportSkipReason, Integer> skipCounts) {
		return skipCounts.entrySet().stream()
				.map(entry -> new DividendSkipSummary(entry.getKey(), entry.getValue()))
				.toList();
	}

	private Map<DividendImportSkipReason, Integer> skipCounts(DividendImportSkipReason reason) {
		Map<DividendImportSkipReason, Integer> counts = new EnumMap<>(DividendImportSkipReason.class);
		counts.put(reason, 1);
		return counts;
	}

	private void logSkip(SecurityItem securityItem, ImportedDividendEvent importedEvent, DividendImportSkipReason skipReason) {
		log.info("Stock dividend import item skipped. securityId={} name={} reason={} searchTerm={} recordDate={} paymentDate={} dividendPerShare={} companyName={} isinName={} stockType={} dividendRecord={}",
				securityItem.getId(),
				securityItem.getName(),
				skipReason,
				importedEvent.searchTerm(),
				importedEvent.recordDate(),
				importedEvent.paymentDate(),
				importedEvent.dividendPerShare(),
				importedEvent.companyName(),
				importedEvent.isinName(),
				importedEvent.stockTypeName(),
				importedEvent.dividendRecordName());
	}

	private enum StockKind {
		COMMON,
		PREFERRED,
		UNKNOWN
	}

	private record StockClass(StockKind kind, int rank) {
	}

	private record MatchDecision(boolean matched, DividendImportSkipReason skipReason) {

		private static MatchDecision match() {
			return new MatchDecision(true, null);
		}

		private static MatchDecision skip(DividendImportSkipReason reason) {
			return new MatchDecision(false, reason);
		}
	}

	private class ImportAccumulator {

		private final int httpStatus;
		private final String resultCode;
		private final String resultMsg;
		private final int totalCount;
		private final int itemCount;
		private final Map<DividendImportSkipReason, Integer> skipCounts = new EnumMap<>(DividendImportSkipReason.class);
		private int importedCount;
		private int skippedCount;
		private int generatedPaymentCount;

		private ImportAccumulator(List<StockDividendFetchResult> fetchResults) {
			this.httpStatus = fetchResults.stream().mapToInt(StockDividendFetchResult::httpStatus).filter(status -> status > 0).reduce((first, second) -> second).orElse(0);
			this.resultCode = fetchResults.stream().map(StockDividendFetchResult::resultCode).filter(StringUtils::hasText).reduce((first, second) -> second).orElse("");
			this.resultMsg = fetchResults.stream().map(StockDividendFetchResult::resultMsg).filter(StringUtils::hasText).reduce((first, second) -> second).orElse("");
			this.totalCount = fetchResults.stream().mapToInt(StockDividendFetchResult::totalCount).sum();
			this.itemCount = fetchResults.stream().mapToInt(StockDividendFetchResult::itemCount).sum();
		}

		private void skip(DividendImportSkipReason reason) {
			skippedCount++;
			skipCounts.merge(reason, 1, Integer::sum);
		}

		private DividendSecurityImportResult toSecurityResult(SecurityItem securityItem, List<String> searchTerms, String status, String message) {
			return securityResult(
					securityItem,
					searchTerms,
					httpStatus == 0 ? null : httpStatus,
					resultCode,
					resultMsg,
					totalCount,
					itemCount,
					importedCount,
					skippedCount,
					generatedPaymentCount,
					status,
					message,
					skipCounts
			);
		}
	}
}
