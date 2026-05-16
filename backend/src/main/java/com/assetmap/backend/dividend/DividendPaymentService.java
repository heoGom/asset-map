package com.assetmap.backend.dividend;

import com.assetmap.backend.account.Account;
import com.assetmap.backend.account.AccountService;
import com.assetmap.backend.common.exception.BusinessException;
import com.assetmap.backend.common.exception.ErrorCode;
import com.assetmap.backend.securityitem.SecurityItem;
import com.assetmap.backend.securityitem.SecurityItemService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DividendPaymentService {

	private final DividendPaymentRepository paymentRepository;
	private final AccountService accountService;
	private final SecurityItemService securityItemService;
	private final DividendEventService dividendEventService;

	public DividendPaymentService(DividendPaymentRepository paymentRepository, AccountService accountService, SecurityItemService securityItemService, DividendEventService dividendEventService) {
		this.paymentRepository = paymentRepository;
		this.accountService = accountService;
		this.securityItemService = securityItemService;
		this.dividendEventService = dividendEventService;
	}

	@Transactional
	public DividendPaymentResponse create(DividendPaymentCreateRequest request) {
		Account account = accountService.getAccount(request.accountId());
		SecurityItem securityItem = securityItemService.getSecurityItem(request.securityItemId());
		DividendEvent event = request.dividendEventId() == null ? null : dividendEventService.getDividendEvent(request.dividendEventId());
		DividendPayment payment = new DividendPayment(request.userId(), account, securityItem, event, request.quantityAtRecordDate(), request.dividendPerShare(), request.taxAmount(), request.currency(), request.exchangeRate(), request.paymentDate(), request.status());
		return DividendPaymentResponse.from(paymentRepository.save(payment));
	}

	public List<DividendPaymentResponse> findAll() {
		return paymentRepository.findAll().stream().map(DividendPaymentResponse::from).toList();
	}

	public DividendPaymentResponse findById(Long paymentId) {
		return DividendPaymentResponse.from(getDividendPayment(paymentId));
	}

	public List<DividendPaymentResponse> findByUserId(Long userId) {
		return paymentRepository.findByUserId(userId).stream().map(DividendPaymentResponse::from).toList();
	}

	@Transactional
	public DividendPaymentResponse update(Long paymentId, DividendPaymentUpdateRequest request) {
		DividendPayment payment = getDividendPayment(paymentId);
		Account account = request.accountId() == null ? null : accountService.getAccount(request.accountId());
		SecurityItem securityItem = request.securityItemId() == null ? null : securityItemService.getSecurityItem(request.securityItemId());
		DividendEvent event = request.dividendEventId() == null ? null : dividendEventService.getDividendEvent(request.dividendEventId());
		payment.update(request, account, securityItem, event);
		return DividendPaymentResponse.from(payment);
	}

	@Transactional
	public void delete(Long paymentId) {
		paymentRepository.delete(getDividendPayment(paymentId));
	}

	public DividendPayment getDividendPayment(Long paymentId) {
		return paymentRepository.findById(paymentId).orElseThrow(() -> new BusinessException(ErrorCode.COMMON_002));
	}
}
