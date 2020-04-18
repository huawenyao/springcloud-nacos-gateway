package com.autel.cloud.paas.gateway;

import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;

public class GatewayAttribute {

	public static final String IS_URL_TOKEN_IGNORED = qualify(
			"isURLTokenIgnored");

	public static final String SERVICE_INSTACNE = qualify(
			"serviceInstance");

	public static final String OPEN_TRACING_SPAN = qualify(
			"openTracingSpan");

	private static String qualify(String attr) {
		return ServerWebExchangeUtils.class.getName() + "." + attr;
	}
}
