package com.autel.cloud.paas.gateway.configuration;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.utils.JSONUtils;
import com.autel.cloud.paas.gateway.route.DynamicRouteServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executor;

@Slf4j
//@Component  //暂不启用
public class DynamicRouteServiceImplByNacos implements CommandLineRunner {

    @Autowired
    private DynamicRouteServiceImpl dynamicRouteService;

    @Autowired
    private NacosGatewayProperties nacosGatewayProperties;


    /**
     * 监听Nacos Server下发的动态路由配置
     */
    public void dynamicRouteByNacosListener (){
        try {
            ConfigService configService= NacosFactory.createConfigService(nacosGatewayProperties.getAddress());
            String content = configService.getConfig(nacosGatewayProperties.getDataId(), nacosGatewayProperties.getGroupId(), nacosGatewayProperties.getTimeout());
            System.out.println(content);
            configService.addListener(nacosGatewayProperties.getDataId(), nacosGatewayProperties.getGroupId(), new Listener()  {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    List<RouteDefinition> list = JSON.parseArray(configInfo, RouteDefinition.class);
                    list.forEach(definition->{
                        dynamicRouteService.update(definition);
                    });
                }
                @Override
                public Executor getExecutor() {
                    return null;
                }
            });
        } catch (NacosException e) {
            log.error("NacosException",e);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        dynamicRouteByNacosListener();

    }

}