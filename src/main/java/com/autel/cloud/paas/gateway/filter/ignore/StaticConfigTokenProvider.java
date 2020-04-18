package com.autel.cloud.paas.gateway.filter.ignore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticConfigTokenProvider implements TokenIgnoreProvider{

	private Map<String,List<String>> urlMap = new HashMap<>();

	public StaticConfigTokenProvider(IgnoreProperties ignoreProperties) {
		Map<String, String> urlMap = ignoreProperties.getUrlMap();
		for (Map.Entry<String, String> entry : urlMap.entrySet()) {
			String key = entry.getKey();
			List<String> strings = Arrays.asList(entry.getValue().split(","));
			this.urlMap.put(key.toLowerCase(),strings);
		}
	}

	@Override
	public List<String> getIgnoredUrls(String serviceId) {
		return urlMap.get(serviceId);
	}
}
