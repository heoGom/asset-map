package com.assetmap.backend.securityitem;

import com.assetmap.backend.common.exception.BusinessException;
import com.assetmap.backend.common.exception.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SecurityItemService {

	private final SecurityItemRepository securityItemRepository;

	public SecurityItemService(SecurityItemRepository securityItemRepository) {
		this.securityItemRepository = securityItemRepository;
	}

	@Transactional
	public SecurityItemResponse create(SecurityItemCreateRequest request) {
		SecurityItem securityItem = new SecurityItem(
				request.ticker(),
				request.name(),
				request.market(),
				request.country(),
				request.currency(),
				request.securityType()
		);
		return SecurityItemResponse.from(securityItemRepository.save(securityItem));
	}

	public List<SecurityItemResponse> findAll() {
		return securityItemRepository.findAll().stream().map(SecurityItemResponse::from).toList();
	}

	public SecurityItemResponse findById(Long securityId) {
		return SecurityItemResponse.from(getSecurityItem(securityId));
	}

	@Transactional
	public SecurityItemResponse update(Long securityId, SecurityItemUpdateRequest request) {
		SecurityItem securityItem = getSecurityItem(securityId);
		securityItem.update(request);
		return SecurityItemResponse.from(securityItem);
	}

	@Transactional
	public void delete(Long securityId) {
		securityItemRepository.delete(getSecurityItem(securityId));
	}

	public SecurityItem getSecurityItem(Long securityId) {
		return securityItemRepository.findById(securityId)
				.orElseThrow(() -> new BusinessException(ErrorCode.COMMON_002));
	}
}
