package com.autel.cloud.paas.gateway.filter.ignore;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("com.taimei.gateway.ignore")
public class IgnoreProperties {

	Map<String,String> urlMap = new HashMap<>();

	public Map<String, String> getUrlMap() {
		return urlMap;
	}

	public void setUrlMap(Map<String, String> urlMap) {
		this.urlMap = urlMap;
	}
}
