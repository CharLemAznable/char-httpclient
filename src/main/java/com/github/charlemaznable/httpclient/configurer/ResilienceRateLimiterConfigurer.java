package com.github.charlemaznable.httpclient.configurer;

import com.github.charlemaznable.httpclient.common.ResilienceRateLimiterRecover;
import io.github.resilience4j.ratelimiter.RateLimiter;

public interface ResilienceRateLimiterConfigurer extends Configurer {

    RateLimiter rateLimiter(String defaultName);

    default <T> ResilienceRateLimiterRecover<T> rateLimiterRecover() {
        return null;
    }
}
