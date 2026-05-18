package com.assetmap.backend.dividend.importer.service;

import com.assetmap.backend.securityitem.SecurityItem;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DividendSearchTermResolver {

	private static final Pattern PREFERRED_SUFFIX = Pattern.compile("(.+?)([2-9]?우B?|우선주?)$");
	private static final Map<String, String> SEARCH_NAME_MAPPING = Map.of(
			"현대차", "현대자동차",
			"현대차우", "현대자동차",
			"현대차2우B", "현대자동차",
			"현대차3우B", "현대자동차"
	);

	public List<String> resolve(SecurityItem securityItem) {
		List<String> candidates = new ArrayList<>();
		add(candidates, securityItem.getName());

		String mapped = SEARCH_NAME_MAPPING.get(securityItem.getName());
		if (StringUtils.hasText(mapped)) {
			add(candidates, mapped);
		}

		String preferredBase = preferredBaseName(securityItem.getName());
		if (StringUtils.hasText(preferredBase)) {
			add(candidates, SEARCH_NAME_MAPPING.getOrDefault(preferredBase, preferredBase));
		}

		return distinct(candidates);
	}

	String preferredBaseName(String securityName) {
		if (!StringUtils.hasText(securityName)) {
			return "";
		}
		Matcher matcher = PREFERRED_SUFFIX.matcher(securityName.trim());
		if (!matcher.matches()) {
			return "";
		}
		return matcher.group(1);
	}

	private void add(List<String> candidates, String value) {
		if (StringUtils.hasText(value)) {
			candidates.add(value.trim());
		}
	}

	private List<String> distinct(List<String> candidates) {
		return new ArrayList<>(new LinkedHashSet<>(candidates));
	}
}
