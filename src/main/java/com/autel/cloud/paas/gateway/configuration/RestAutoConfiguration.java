package com.autel.cloud.paas.gateway.configuration;

import com.autel.cloud.paas.gateway.common.AuthProperties;
import com.autel.cloud.paas.gateway.filter.AuthorizeGatewayFilterFactory;
import com.autel.cloud.paas.gateway.filter.ResourceGatewayFilterFactory;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.codec.ServerCodecConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RestAutoConfiguration {

	private final ApplicationContext applicationContext;

	private final ResourceProperties resourceProperties;

	private final ServerCodecConfigurer serverCodecConfigurer;

	public RestAutoConfiguration(ServerProperties serverProperties, ApplicationContext applicationContext, ResourceProperties resourceProperties, ServerCodecConfigurer serverCodecConfigurer) {
		this.applicationContext = applicationContext;
		this.resourceProperties = resourceProperties;
		this.serverCodecConfigurer = serverCodecConfigurer;
	}


	@Bean
	public AuthProperties authProperties() {
		return new AuthProperties();
	}

//	@Bean
//	public AuthorizeGatewayFilterFactory cspTokenGatewayFilterFactory(ReactiveStringRedisTemplate reactiveStringRedisTemplate,
//																	  AuthProperties authProperties) {
//		return new AuthorizeGatewayFilterFactory(reactiveStringRedisTemplate, authProperties);
//	}
//
//	@Bean
//	public ResourceGatewayFilterFactory resourceGatewayFilterFactory(ReactiveStringRedisTemplate reactiveStringRedisTemplate){
//		return new ResourceGatewayFilterFactory(reactiveStringRedisTemplate);
//	}

	//--------------global------------------//
	@Bean
	@Primary
	public ReactiveStringRedisTemplate reactiveStringRedisTemplate(
			ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
		RedisSerializer<String> serializer = new StringRedisSerializer();
		RedisSerializationContext<String, String> serializationContext = RedisSerializationContext
				.<String, String>newSerializationContext().key(serializer)
				.value(serializer).hashKey(serializer).hashValue(serializer).build();
		return new ReactiveStringRedisTemplate(reactiveRedisConnectionFactory,
				serializationContext);
	}

}
