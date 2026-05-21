package com.assetmap.backend.datasync.provider;

import com.assetmap.backend.datasync.common.DataSyncSource;
import com.assetmap.backend.securityitem.SecurityType;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class KrxSecurityMasterProvider implements SecurityMasterProvider {

	private static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.BASIC_ISO_DATE;

	private final KrxApiClient krxApiClient;
	private final String kospiUrl;
	private final String kosdaqUrl;

	public KrxSecurityMasterProvider(
			KrxApiClient krxApiClient,
			@Value("${external.krx.security-master.kospi-url}") String kospiUrl,
			@Value("${external.krx.security-master.kosdaq-url}") String kosdaqUrl
	) {
		this.krxApiClient = krxApiClient;
		this.kospiUrl = kospiUrl;
		this.kosdaqUrl = kosdaqUrl;
	}

	@Override
	public List<ImportedSecurityMaster> fetchAllKospi(LocalDate basDd) {
		return fetch("KOSPI_SECURITY_MASTER", kospiUrl, basDd);
	}

	@Override
	public List<ImportedSecurityMaster> fetchAllKosdaq(LocalDate basDd) {
		return fetch("KOSDAQ_SECURITY_MASTER", kosdaqUrl, basDd);
	}

	@Override
	public List<ImportedSecurityMaster> fetchAllEtf(LocalDate basDd) {
		return List.of();
	}

	private List<ImportedSecurityMaster> fetch(String endpointName, String url, LocalDate basDd) {
		return krxApiClient.postForOutBlockItems(endpointName, url, basDd)
				.stream()
				.map(this::toImportedSecurityMaster)
				.toList();
	}

	private ImportedSecurityMaster toImportedSecurityMaster(JsonNode item) {
		String shortName = text(item, "ISU_ABBRV");
		String fullName = text(item, "ISU_NM");
		String name = StringUtils.hasText(shortName) ? shortName : fullName;
		return new ImportedSecurityMaster(
				text(item, "ISU_SRT_CD"),
				text(item, "ISU_CD"),
				name,
				shortName,
				text(item, "ISU_ENG_NM"),
				text(item, "MKT_TP_NM"),
				SecurityType.STOCK,
				parseDate(text(item, "LIST_DD")),
				"KRW",
				DataSyncSource.KRX
		);
	}

	private String text(JsonNode node, String fieldName) {
		String value = node.path(fieldName).asText("");
		if (!StringUtils.hasText(value)) {
			return "";
		}
		String trimmed = value.trim();
		return "-".equals(trimmed) ? "" : trimmed;
	}

	private LocalDate parseDate(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		try {
			return LocalDate.parse(value.trim(), COMPACT_DATE);
		} catch (DateTimeParseException exception) {
			return null;
		}
	}
}
