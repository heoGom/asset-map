package com.assetmap.backend.transaction;

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
public class TradeTransactionService {

	private final TradeTransactionRepository transactionRepository;
	private final AccountService accountService;
	private final SecurityItemService securityItemService;
	private final HoldingAdjustmentService holdingAdjustmentService;

	public TradeTransactionService(TradeTransactionRepository transactionRepository, AccountService accountService, SecurityItemService securityItemService, HoldingAdjustmentService holdingAdjustmentService) {
		this.transactionRepository = transactionRepository;
		this.accountService = accountService;
		this.securityItemService = securityItemService;
		this.holdingAdjustmentService = holdingAdjustmentService;
	}

	@Transactional
	public TradeTransactionResponse create(TradeTransactionCreateRequest request) {
		Account account = accountService.getAccount(request.accountId());
		SecurityItem securityItem = securityItemService.getSecurityItem(request.securityItemId());
		if (request.tradeType() == TradeType.SELL) {
			holdingAdjustmentService.validateSellable(request.userId(), account.getId(), securityItem.getId(), request.quantity());
		}
		TradeTransaction transaction = transactionRepository.save(new TradeTransaction(request.userId(), account, securityItem, request.tradeDate(), request.tradeType(), request.quantity(), request.price(), request.fee(), request.tax(), request.currency(), request.source(), request.memo()));
		holdingAdjustmentService.rebuild(transaction.getUserId(), account.getId(), securityItem.getId());
		return TradeTransactionResponse.from(transaction);
	}

	public List<TradeTransactionResponse> findAll(Long userId) {
		List<TradeTransaction> transactions = userId == null ? transactionRepository.findAll() : transactionRepository.findByUserIdOrderByTradeDateAscIdAsc(userId);
		return transactions.stream().map(TradeTransactionResponse::from).toList();
	}

	public TradeTransactionResponse findById(Long tradeId) {
		return TradeTransactionResponse.from(getTradeTransaction(tradeId));
	}

	@Transactional
	public TradeTransactionResponse update(Long tradeId, TradeTransactionUpdateRequest request) {
		TradeTransaction transaction = getTradeTransaction(tradeId);
		PositionKey oldKey = PositionKey.from(transaction);
		Account account = request.accountId() == null ? null : accountService.getAccount(request.accountId());
		SecurityItem securityItem = request.securityItemId() == null ? null : securityItemService.getSecurityItem(request.securityItemId());
		transaction.update(request, account, securityItem);
		holdingAdjustmentService.rebuild(oldKey.userId(), oldKey.accountId(), oldKey.securityItemId());
		holdingAdjustmentService.rebuild(transaction.getUserId(), transaction.getAccount().getId(), transaction.getSecurityItem().getId());
		return TradeTransactionResponse.from(transaction);
	}

	@Transactional
	public void delete(Long tradeId) {
		TradeTransaction transaction = getTradeTransaction(tradeId);
		PositionKey key = PositionKey.from(transaction);
		transactionRepository.delete(transaction);
		holdingAdjustmentService.rebuild(key.userId(), key.accountId(), key.securityItemId());
	}

	public TradeTransaction getTradeTransaction(Long tradeId) {
		return transactionRepository.findById(tradeId).orElseThrow(() -> new BusinessException(ErrorCode.COMMON_002));
	}

	private record PositionKey(Long userId, Long accountId, Long securityItemId) {
		private static PositionKey from(TradeTransaction transaction) {
			return new PositionKey(transaction.getUserId(), transaction.getAccount().getId(), transaction.getSecurityItem().getId());
		}
	}
}
