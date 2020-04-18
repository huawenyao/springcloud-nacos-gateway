package com.autel.cloud.paas.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PaasGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaasGatewayApplication.class, args);
    }

}
