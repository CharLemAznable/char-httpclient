package com.github.charlemaznable.httpclient.ohclient.configurer;

import com.github.charlemaznable.httpclient.configurer.Configurer;

import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_MAX_REQUESTS;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_MAX_REQUESTS_PER_HOST;

public interface ClientDispatcherConfigurer extends Configurer {

    default int maxRequests() {
        return DEFAULT_MAX_REQUESTS;
    }

    default int maxRequestsPerHost() {
        return DEFAULT_MAX_REQUESTS_PER_HOST;
    }
}
