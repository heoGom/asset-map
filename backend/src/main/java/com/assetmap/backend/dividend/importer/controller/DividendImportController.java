package com.assetmap.backend.dividend.importer.controller;

import com.assetmap.backend.auth.SecurityUtil;
import com.assetmap.backend.common.response.ApiResponse;
import com.assetmap.backend.dividend.importer.dto.DividendImportRequest;
import com.assetmap.backend.dividend.importer.dto.DividendImportResult;
import com.assetmap.backend.dividend.importer.service.DividendEventImportService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dividends/import/public-data/stock")
public class DividendImportController {

	private final DividendEventImportService dividendEventImportService;

	public DividendImportController(DividendEventImportService dividendEventImportService) {
		this.dividendEventImportService = dividendEventImportService;
	}

	@PostMapping("/my-securities")
	public ApiResponse<DividendImportResult> importMySecurities(@Valid @RequestBody(required = false) DividendImportRequest request) {
		return ApiResponse.success(dividendEventImportService.importMySecurities(SecurityUtil.getCurrentUserId(), request));
	}

	@PostMapping
	public ApiResponse<DividendImportResult> importOne(@Valid @RequestBody DividendImportRequest request) {
		return ApiResponse.success(dividendEventImportService.importOne(SecurityUtil.getCurrentUserId(), request));
	}
}
