package com.assetmap.backend.account;

import com.assetmap.backend.common.exception.BusinessException;
import com.assetmap.backend.common.exception.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AccountService {

	private final AccountRepository accountRepository;

	public AccountService(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Transactional
	public AccountResponse create(Long userId, AccountCreateRequest request) {
		Account account = new Account(
				userId,
				request.name(),
				request.brokerName(),
				request.accountType(),
				request.currency(),
				request.memo()
		);
		return AccountResponse.from(accountRepository.save(account));
	}

	public List<AccountResponse> findAll(Long userId) {
		return accountRepository.findByUserId(userId).stream().map(AccountResponse::from).toList();
	}

	public AccountResponse findById(Long userId, Long accountId) {
		return AccountResponse.from(getAccountForUser(userId, accountId));
	}

	@Transactional
	public AccountResponse update(Long userId, Long accountId, AccountUpdateRequest request) {
		Account account = getAccountForUser(userId, accountId);
		account.update(request);
		return AccountResponse.from(account);
	}

	@Transactional
	public void delete(Long userId, Long accountId) {
		accountRepository.delete(getAccountForUser(userId, accountId));
	}

	public Account getAccount(Long accountId) {
		return accountRepository.findById(accountId)
				.orElseThrow(() -> new BusinessException(ErrorCode.COMMON_002));
	}

	public Account getAccountForUser(Long userId, Long accountId) {
		return accountRepository.findByIdAndUserId(accountId, userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.AUTH_004));
	}
}
