package com.autel.cloud.paas.gateway.test.rest;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController  // 提供一个简单的降级页面
public class TestController {


    /**
     * @Title: demo
     * @Description: 一个简单的降级页面
     * @return
     */
    @RequestMapping("/demo")
    public Mono<String> fallback() {
        // Mono是一个Reactive stream，对外输出一个“demo”字符串。
        return Mono.just("demo");
    }


}