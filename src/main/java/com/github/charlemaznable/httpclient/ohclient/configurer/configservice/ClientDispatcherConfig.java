package com.github.charlemaznable.httpclient.ohclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientDispatcherConfigurer;
import org.apache.commons.lang3.math.NumberUtils;

import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_MAX_REQUESTS;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_MAX_REQUESTS_PER_HOST;

public interface ClientDispatcherConfig extends ClientDispatcherConfigurer {

    @Config("maxRequests")
    String maxRequestsString();

    @Config("maxRequestsPerHost")
    String maxRequestsPerHostString();

    @Override
    default int maxRequests() {
        return NumberUtils.toInt(maxRequestsString(), DEFAULT_MAX_REQUESTS);
    }

    @Override
    default int maxRequestsPerHost() {
        return NumberUtils.toInt(maxRequestsPerHostString(), DEFAULT_MAX_REQUESTS_PER_HOST);
    }
}
