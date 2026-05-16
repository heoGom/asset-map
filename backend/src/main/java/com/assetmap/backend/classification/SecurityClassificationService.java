package com.assetmap.backend.classification;

import com.assetmap.backend.common.exception.BusinessException;
import com.assetmap.backend.common.exception.ErrorCode;
import com.assetmap.backend.securityitem.SecurityItem;
import com.assetmap.backend.securityitem.SecurityItemService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SecurityClassificationService {

	private final SecurityClassificationRepository classificationRepository;
	private final SecurityItemService securityItemService;

	public SecurityClassificationService(
			SecurityClassificationRepository classificationRepository,
			SecurityItemService securityItemService
	) {
		this.classificationRepository = classificationRepository;
		this.securityItemService = securityItemService;
	}

	@Transactional
	public SecurityClassificationResponse create(SecurityClassificationCreateRequest request) {
		SecurityItem securityItem = securityItemService.getSecurityItem(request.securityItemId());
		if (classificationRepository.existsBySecurityItem(securityItem)) {
			throw new BusinessException(ErrorCode.COMMON_001);
		}
		SecurityClassification classification = new SecurityClassification(
				securityItem,
				request.countryGroup(),
				request.assetGroup(),
				request.sector(),
				request.strategyType(),
				request.theme(),
				request.listingCountry(),
				request.exposureCountry(),
				request.exposureRegion(),
				request.tradingCurrency(),
				request.currencyExposure(),
				request.underlyingIndex(),
				request.hedged()
		);
		return SecurityClassificationResponse.from(classificationRepository.save(classification));
	}

	public List<SecurityClassificationResponse> findAll() {
		return classificationRepository.findAll().stream().map(SecurityClassificationResponse::from).toList();
	}

	public SecurityClassificationResponse findById(Long classificationId) {
		return SecurityClassificationResponse.from(getClassification(classificationId));
	}

	public SecurityClassificationResponse findBySecurityItemId(Long securityItemId) {
		return SecurityClassificationResponse.from(classificationRepository.findBySecurityItemId(securityItemId)
				.orElseThrow(() -> new BusinessException(ErrorCode.COMMON_002)));
	}

	@Transactional
	public SecurityClassificationResponse update(Long classificationId, SecurityClassificationUpdateRequest request) {
		SecurityClassification classification = getClassification(classificationId);
		classification.update(request);
		return SecurityClassificationResponse.from(classification);
	}

	@Transactional
	public void delete(Long classificationId) {
		classificationRepository.delete(getClassification(classificationId));
	}

	public SecurityClassification getClassification(Long classificationId) {
		return classificationRepository.findById(classificationId)
				.orElseThrow(() -> new BusinessException(ErrorCode.COMMON_002));
	}
}
