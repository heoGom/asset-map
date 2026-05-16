package com.assetmap.backend.dividend;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DividendEventRepository extends JpaRepository<DividendEvent, Long> {

	List<DividendEvent> findBySecurityItemId(Long securityItemId);

	List<DividendEvent> findBySecurityItemIdAndDividendYear(Long securityItemId, Integer dividendYear);
}
