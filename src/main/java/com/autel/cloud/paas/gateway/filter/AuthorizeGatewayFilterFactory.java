package com.autel.cloud.paas.gateway.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.autel.cloud.paas.gateway.SystemConstant;
import com.autel.cloud.paas.gateway.common.AuthProperties;
import com.autel.cloud.paas.gateway.util.SerializeUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class AuthorizeGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthorizeGatewayFilterFactory.Config> {

    //request param  key
    private static final String AUTHORIZE_TOKEN = SystemConstant.HEADER_TOKEN;
    //redis user token key
    private static final String AUTHORIZE_UID = "uid";

    @Autowired
    private ReactiveStringRedisTemplate stringRedisTemplate;
    @Autowired
    private AuthProperties authProperties;

    public AuthorizeGatewayFilterFactory() {
        super(Config.class);
        log.info("Loaded GatewayFilterFactory [Authorize]");
    }

    public AuthorizeGatewayFilterFactory(ReactiveStringRedisTemplate reactiveStringRedisTemplate, AuthProperties authPropertie) {
        super(Config.class);
        this.authProperties = authPropertie;
        this.stringRedisTemplate = reactiveStringRedisTemplate;
        log.info("Loaded GatewayFilterFactory [Authorize]");
    }

    @Override
    public GatewayFilter apply(AuthorizeGatewayFilterFactory.Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            HttpHeaders headers = request.getHeaders();
            String token = headers.getFirst(AUTHORIZE_TOKEN);
            String uid = headers.getFirst(AUTHORIZE_UID);
            if (token == null) {
                token = request.getQueryParams().getFirst(AUTHORIZE_TOKEN);
            }
            if (uid == null) {
                uid = request.getQueryParams().getFirst(AUTHORIZE_UID);
            }

            ServerHttpResponse response = exchange.getResponse();
            if (StrUtil.isEmpty(token) || StrUtil.isEmpty(uid)) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
            Mono<String> authToken = stringRedisTemplate.opsForValue().get(uid);

            if (authToken == null || !authToken.equals(token)) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
            Mono<ServerHttpRequest.Builder> mononew= addSysUserInfo2Header(token,exchange,request);

            return mononew.flatMap(mu -> {
                ServerWebExchange newExchange = exchange.mutate().request(mu.build()).build();
                return chain.filter(newExchange);
                });
        };

    }

    private Mono<ServerHttpRequest.Builder> addSysUserInfo2Header(String token, ServerWebExchange exchange, ServerHttpRequest request) {
        /**
         * redis中token 拼接规则
         */
        String redisTokenKey = "sso:token:" + token;
        AtomicReference<Mono<ServerHttpRequest.Builder>> mutateMono = new AtomicReference<>();
        try {
            Mono<String> userInfoMono = stringRedisTemplate.opsForValue().get(redisTokenKey);
            return userInfoMono.defaultIfEmpty("")
                    .onErrorResume(throwable -> {
                        log.error("error request sso session", throwable);
                        return Mono.just("");
                    })
                    .flatMap(tokenJson -> {

                        Map<String, Object> userData = SerializeUtils.fromJson(tokenJson, new TypeReference<Map<String, Object>>() {
                        });
                        if (userData == null) {
                            log.info("unable to deserialize session info");
                            exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                        }

                        String tenantId = (String) userData.get(SystemConstant.HEADER_TENANT_ID);


                        ServerHttpRequest.Builder mutate = addHeaderForUserData(request, userData, redisTokenKey, token);
                        mutateMono.set(Mono.just(mutate));
                        return mutateMono.get();
                    });
        } catch (Exception ex) {
            log.error("wtf ", ex);
        }
        return mutateMono.get();
    }

    private ServerHttpRequest.Builder addHeaderForUserData(ServerHttpRequest request, Map<String, Object> userData, String redisTokenKey, String token) {

        if (authProperties.getExpireTimeInSeconds() > 0) {

            stringRedisTemplate.expire(redisTokenKey, Duration.ofSeconds(authProperties.getExpireTimeInSeconds()))
                    .onErrorResume(throwable -> {
                        log.error("refresh expire error", throwable);
                        return Mono.empty();
                    }).subscribe();
        }

        ServerHttpRequest.Builder mutate = request.mutate();
        mutate.header(SystemConstant.HEADER_TOKEN, token);
        String userId = (String) userData.get("userId");
        if (userId != null) {
            mutate = mutate.header(SystemConstant.HEADER_USER_ID, userId);
        }
        String accountId = (String) userData.get("accountId");
        if (accountId != null) {
            mutate = mutate.header(SystemConstant.HEADER_ACCOUNT_ID, accountId);
        }
        try {
            String accountName = (String) userData.get("accountName");
            if (accountName != null) {
                mutate = mutate.header(SystemConstant.HEADER_ACCOUNT_NAME, URLEncoder.encode(accountName, "utf-8"));
            }
            String userName = (String) userData.get("userName");
            if (userName != null) {
                mutate = mutate.header(SystemConstant.HEADER_USER_NAME, URLEncoder.encode(userName, "utf-8"));
            }
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("encoding failed");
        }
        String tenantId = (String) userData.get("tenantId");
        if (tenantId != null) {
            mutate = mutate.header(SystemConstant.HEADER_TENANT_ID, tenantId);
        }
        String platform = (String) userData.get("platform");
        if (platform != null) {
            mutate = mutate.header(SystemConstant.HEADER_PLATFORM, platform);
        }
        String device = (String) userData.get("device");
        if (device != null) {
            mutate = mutate.header(SystemConstant.HEADER_DEVICE, device);
        }
        List<String> roleIds = (List) userData.get("roleIds");
        if (CollUtil.isNotEmpty(roleIds)) {
            mutate = mutate.header(SystemConstant.HEADER_PLATFORM, roleIds.toArray(new String[0]));
        }
        //其他用户熟悉也可以解析出来 放到header 传递到后端服务
        return mutate;
    }
    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("enabled");
    }

    public static class Config {
        // 控制是否开启认证
        private boolean enabled;

        public Config() {
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
