package com.autel.cloud.paas.gateway.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class ServerWebExchangeUtil {

	public static Mono<Void> completeWithCode(ServerWebExchange exchange, HttpStatus httpStatus) {
		exchange.getResponse().setStatusCode(httpStatus);
		return  exchange.getResponse().setComplete();
	}
}
