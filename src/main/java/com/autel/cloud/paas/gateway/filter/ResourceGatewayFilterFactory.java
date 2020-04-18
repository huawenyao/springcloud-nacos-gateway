package com.autel.cloud.paas.gateway.filter;

import cn.hutool.core.util.StrUtil;
import com.autel.cloud.paas.gateway.SystemConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ResourceGatewayFilterFactory extends AbstractGatewayFilterFactory {


    @Autowired
    ReactiveRedisTemplate  reactiveRedisTemplate;

    public ResourceGatewayFilterFactory() {
        super();
    }

    public ResourceGatewayFilterFactory( ReactiveRedisTemplate  reactiveRedisTemplate) {
        super();
        this.reactiveRedisTemplate=reactiveRedisTemplate;
    }


    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            boolean authResult = false;

            authResult = checkResourceURI(request);

            if (!authResult) {
                log.info("sign not right. return http code 401. uri:{}", exchange.getRequest().getURI().toString());
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
            if (log.isDebugEnabled()) {
                log.debug("sign success");
            }
            return chain.filter(exchange);
        };
    }

    /**
     * 提取header中装入的属性 根据配置表过滤
     *
     * @param request
     * @return
     */
    private boolean checkResourceURI(ServerHttpRequest request) {
        String pathRequest = StrUtil.format(request.getPath().value(),
                request.getQueryParams());
        String userId=request.getHeaders().getFirst(SystemConstant.HEADER_USER_ID);
        return false;
    }


}
