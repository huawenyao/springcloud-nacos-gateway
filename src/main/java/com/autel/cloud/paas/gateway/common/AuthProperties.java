package com.autel.cloud.paas.gateway.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("com.autel.cloud.gateway.auth")
public class AuthProperties {


	private int expireTimeInSeconds = 3600 * 24;

	private int expireTimeAppInSeconds = 3600 * 24 * 30;


	public int getExpireTimeInSeconds() {
		return expireTimeInSeconds;
	}

	public void setExpireTimeInSeconds(int expireTimeInSeconds) {
		this.expireTimeInSeconds = expireTimeInSeconds;
	}

	public int getExpireTimeAppInSeconds() {
		return expireTimeAppInSeconds;
	}

	public void setExpireTimeAppInSeconds(int expireTimeAppInSeconds) {
		this.expireTimeAppInSeconds = expireTimeAppInSeconds;
	}
}
