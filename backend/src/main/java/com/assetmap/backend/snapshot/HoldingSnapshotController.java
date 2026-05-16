package com.assetmap.backend.snapshot;

import com.assetmap.backend.common.response.ApiResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/snapshots")
public class HoldingSnapshotController {

	private final HoldingSnapshotService snapshotService;

	public HoldingSnapshotController(HoldingSnapshotService snapshotService) {
		this.snapshotService = snapshotService;
	}

	@PostMapping
	public ApiResponse<SnapshotSaveResponse> save(@Valid @RequestBody SnapshotSaveRequest request) {
		return ApiResponse.success(snapshotService.saveCurrentHoldings(request));
	}

	@GetMapping("/timeline")
	public ApiResponse<List<AssetTimelineResponse>> timeline(
			@RequestParam Long userId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
	) {
		return ApiResponse.success(snapshotService.timeline(userId, from, to));
	}

	@GetMapping("/by-account")
	public ApiResponse<List<AccountTimelineResponse>> byAccount(
			@RequestParam Long userId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
	) {
		return ApiResponse.success(snapshotService.byAccount(userId, from, to));
	}
}
