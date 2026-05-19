package com.assetmap.backend.dividend.importer.service;

import com.assetmap.backend.dividend.DataSourceType;
import com.assetmap.backend.dividend.DividendEvent;
import com.assetmap.backend.dividend.DividendEventRepository;
import com.assetmap.backend.dividend.DividendEventType;
import com.assetmap.backend.dividend.DividendPaymentGenerateRequest;
import com.assetmap.backend.dividend.DividendPaymentGenerateResponse;
import com.assetmap.backend.dividend.DividendPaymentService;
import com.assetmap.backend.dividend.importer.dto.DividendImportSkipReason;
import com.assetmap.backend.dividend.importer.dto.ImportedDividendEvent;
import com.assetmap.backend.securityitem.SecurityItem;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DividendEventImportOperationService {

	private final DividendEventRepository dividendEventRepository;
	private final DividendPaymentService dividendPaymentService;

	public DividendEventImportOperationService(
			DividendEventRepository dividendEventRepository,
			DividendPaymentService dividendPaymentService
	) {
		this.dividendEventRepository = dividendEventRepository;
		this.dividendPaymentService = dividendPaymentService;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ImportedDividendOperationResult saveEventAndGeneratePayments(
			Long userId,
			SecurityItem securityItem,
			ImportedDividendEvent importedEvent
	) {
		return saveEventAndGeneratePayments(List.of(userId), securityItem, importedEvent);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ImportedDividendOperationResult saveEventAndGeneratePayments(
			List<Long> userIds,
			SecurityItem securityItem,
			ImportedDividendEvent importedEvent
	) {
		DividendEvent saved;
		try {
			saved = dividendEventRepository.saveAndFlush(new DividendEvent(
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
		} catch (DataAccessException exception) {
			throw new DividendEventImportOperationException(DividendImportSkipReason.SAVE_FAILED, exception);
		}

		return generatePayments(userIds, saved.getId());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ImportedDividendOperationResult generatePayments(List<Long> userIds, Long dividendEventId) {
		int generatedPaymentCount = 0;
		try {
			for (Long userId : userIds) {
				DividendPaymentGenerateResponse generated = dividendPaymentService.generate(new DividendPaymentGenerateRequest(userId, dividendEventId));
				generatedPaymentCount += generated.generatedCount();
			}
			return new ImportedDividendOperationResult(dividendEventId, generatedPaymentCount);
		} catch (RuntimeException exception) {
			throw new DividendEventImportOperationException(DividendImportSkipReason.PAYMENT_GENERATION_FAILED, exception);
		}
	}

	public record ImportedDividendOperationResult(
			Long eventId,
			int generatedPaymentCount
	) {
	}
}
