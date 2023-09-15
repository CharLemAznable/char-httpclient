package com.github.charlemaznable.httpclient.resilience.configurer;

import com.github.charlemaznable.httpclient.configurer.Configurer;
import io.github.resilience4j.retry.Retry;

public interface ResilienceRetryConfigurer extends Configurer {

    Retry retry(String defaultName);

    default boolean isolatedExecutor() {
        return false;
    }
}
