package com.autel.cloud.paas.gateway.filter.ignore;

import java.util.List;

public interface TokenIgnoreProvider {

	List<String> getIgnoredUrls(String serviceId);
}
