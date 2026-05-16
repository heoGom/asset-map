package com.assetmap.backend.marketprice;

import com.assetmap.backend.common.exception.BusinessException;
import com.assetmap.backend.common.exception.ErrorCode;
import com.assetmap.backend.securityitem.SecurityItem;
import com.assetmap.backend.securityitem.SecurityItemService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MarketPriceService {

	private final MarketPriceRepository marketPriceRepository;
	private final SecurityItemService securityItemService;
	private final MarketPriceProvider marketPriceProvider;

	public MarketPriceService(MarketPriceRepository marketPriceRepository, SecurityItemService securityItemService, MarketPriceProvider marketPriceProvider) {
		this.marketPriceRepository = marketPriceRepository;
		this.securityItemService = securityItemService;
		this.marketPriceProvider = marketPriceProvider;
	}

	@Transactional
	public MarketPriceResponse create(MarketPriceCreateRequest request) {
		SecurityItem securityItem = securityItemService.getSecurityItem(request.securityItemId());
		MarketPrice marketPrice = new MarketPrice(
				securityItem,
				request.priceDate(),
				request.closePrice(),
				request.currentPrice(),
				request.changeAmount() == null ? request.currentPrice().subtract(request.closePrice()) : request.changeAmount(),
				request.changeRate() == null ? BigDecimal.ZERO : request.changeRate(),
				request.volume(),
				request.source(),
				LocalDateTime.now()
		);
		return MarketPriceResponse.from(marketPriceRepository.save(marketPrice));
	}

	public List<MarketPriceResponse> findBySecurityItemId(Long securityItemId) {
		return marketPriceRepository.findBySecurityItemIdOrderByPriceDateDesc(securityItemId).stream().map(MarketPriceResponse::from).toList();
	}

	public MarketPriceResponse latest(Long securityItemId) {
		return MarketPriceResponse.from(marketPriceRepository.findFirstBySecurityItemIdOrderByPriceDateDescFetchedAtDesc(securityItemId)
				.orElseThrow(() -> new BusinessException(ErrorCode.COMMON_002)));
	}

	@Transactional
	public MarketPriceResponse refresh(MarketPriceRefreshRequest request) {
		SecurityItem securityItem = securityItemService.getSecurityItem(request.securityItemId());
		return MarketPriceResponse.from(marketPriceRepository.save(marketPriceProvider.fetch(securityItem, request.priceDate())));
	}
}
