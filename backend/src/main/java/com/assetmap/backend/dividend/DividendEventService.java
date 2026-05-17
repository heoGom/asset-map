package com.assetmap.backend.dividend;

import com.assetmap.backend.common.exception.BusinessException;
import com.assetmap.backend.common.exception.ErrorCode;
import com.assetmap.backend.securityitem.SecurityItem;
import com.assetmap.backend.securityitem.SecurityItemService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DividendEventService {

	private final DividendEventRepository dividendEventRepository;
	private final SecurityItemService securityItemService;

	public DividendEventService(DividendEventRepository dividendEventRepository, SecurityItemService securityItemService) {
		this.dividendEventRepository = dividendEventRepository;
		this.securityItemService = securityItemService;
	}

	@Transactional
	public DividendEventResponse create(DividendEventCreateRequest request) {
		SecurityItem securityItem = securityItemService.getSecurityItem(request.securityItemId());
		DividendEvent event = new DividendEvent(securityItem, request.dividendYear(), request.declarationDate(), request.exDividendDate(), request.recordDate(), request.paymentDate(), request.eventType(), request.dividendPerShare(), request.currency(), request.source());
		return DividendEventResponse.from(dividendEventRepository.save(event));
	}

	public List<DividendEventResponse> findAll() {
		return dividendEventRepository.findAll().stream().map(DividendEventResponse::from).toList();
	}

	public DividendEventResponse findById(Long eventId) {
		return DividendEventResponse.from(getDividendEvent(eventId));
	}

	public List<DividendEventResponse> findBySecurityItemId(Long securityItemId) {
		return dividendEventRepository.findBySecurityItemId(securityItemId).stream().map(DividendEventResponse::from).toList();
	}

	@Transactional
	public DividendEventResponse update(Long eventId, DividendEventUpdateRequest request) {
		DividendEvent event = getDividendEvent(eventId);
		SecurityItem securityItem = request.securityItemId() == null ? null : securityItemService.getSecurityItem(request.securityItemId());
		event.update(request, securityItem);
		return DividendEventResponse.from(event);
	}

	@Transactional
	public void delete(Long eventId) {
		dividendEventRepository.delete(getDividendEvent(eventId));
	}

	public DividendEvent getDividendEvent(Long eventId) {
		return dividendEventRepository.findById(eventId).orElseThrow(() -> new BusinessException(ErrorCode.COMMON_002));
	}
}
