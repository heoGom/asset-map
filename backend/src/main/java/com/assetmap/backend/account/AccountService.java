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
	public AccountResponse create(AccountCreateRequest request) {
		Account account = new Account(
				request.userId(),
				request.name(),
				request.brokerName(),
				request.accountType(),
				request.currency(),
				request.memo()
		);
		return AccountResponse.from(accountRepository.save(account));
	}

	public List<AccountResponse> findAll() {
		return accountRepository.findAll().stream().map(AccountResponse::from).toList();
	}

	public AccountResponse findById(Long accountId) {
		return AccountResponse.from(getAccount(accountId));
	}

	@Transactional
	public AccountResponse update(Long accountId, AccountUpdateRequest request) {
		Account account = getAccount(accountId);
		account.update(request);
		return AccountResponse.from(account);
	}

	@Transactional
	public void delete(Long accountId) {
		accountRepository.delete(getAccount(accountId));
	}

	public Account getAccount(Long accountId) {
		return accountRepository.findById(accountId)
				.orElseThrow(() -> new BusinessException(ErrorCode.COMMON_002));
	}
}
