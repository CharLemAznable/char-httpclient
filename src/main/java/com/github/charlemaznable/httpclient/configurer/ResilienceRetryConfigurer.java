package com.github.charlemaznable.httpclient.configurer;

import io.github.resilience4j.retry.Retry;

public interface ResilienceRetryConfigurer extends Configurer {

    Retry retry();

    default boolean isolatedExecutor() {
        return false;
    }
}
