package com.assetmap.backend.holding;

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
public class HoldingService {

	private final HoldingRepository holdingRepository;
	private final AccountService accountService;
	private final SecurityItemService securityItemService;

	public HoldingService(
			HoldingRepository holdingRepository,
			AccountService accountService,
			SecurityItemService securityItemService
	) {
		this.holdingRepository = holdingRepository;
		this.accountService = accountService;
		this.securityItemService = securityItemService;
	}

	@Transactional
	public HoldingResponse create(HoldingCreateRequest request) {
		Account account = accountService.getAccount(request.accountId());
		SecurityItem securityItem = securityItemService.getSecurityItem(request.securityItemId());
		Holding holding = new Holding(
				request.userId(),
				account,
				securityItem,
				request.quantity(),
				request.averagePrice(),
				request.currentPrice(),
				request.currency()
		);
		return HoldingResponse.from(holdingRepository.save(holding));
	}

	public List<HoldingResponse> findAll(Long userId) {
		return holdingRepository.findByUserId(userId).stream().map(HoldingResponse::from).toList();
	}

	public List<HoldingResponse> findByAccount(Long userId, Long accountId) {
		accountService.getAccountForUser(userId, accountId);
		return holdingRepository.findByUserIdAndAccountId(userId, accountId).stream()
				.map(HoldingResponse::from)
				.toList();
	}

	public HoldingResponse findById(Long holdingId) {
		return HoldingResponse.from(getHolding(holdingId));
	}

	@Transactional
	public HoldingResponse update(Long holdingId, HoldingUpdateRequest request) {
		Holding holding = getHolding(holdingId);
		Account account = request.accountId() == null ? null : accountService.getAccount(request.accountId());
		SecurityItem securityItem = request.securityItemId() == null ? null : securityItemService.getSecurityItem(request.securityItemId());
		holding.update(request, account, securityItem);
		return HoldingResponse.from(holding);
	}

	@Transactional
	public void delete(Long holdingId) {
		holdingRepository.delete(getHolding(holdingId));
	}

	public Holding getHolding(Long holdingId) {
		return holdingRepository.findById(holdingId)
				.orElseThrow(() -> new BusinessException(ErrorCode.COMMON_002));
	}
}
