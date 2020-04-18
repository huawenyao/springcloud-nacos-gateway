package com.autel.cloud.paas.gateway.filter;

import com.autel.cloud.paas.gateway.SystemConstant;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局检查token
 */
@Component
public class GlobalTokenFilter implements GlobalFilter, Ordered {


    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getQueryParams().getFirst( SystemConstant.HEADER_TOKEN);
//        if (token == null || token.isEmpty()) {
//            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//            return exchange.getResponse().setComplete();
//        }
        return chain.filter(exchange);
    }


    public int getOrder() {
        return -100;
    }
}