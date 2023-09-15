package com.github.charlemaznable.httpclient.resilience.configurer;

import com.github.charlemaznable.httpclient.resilience.function.ResilienceRateLimiterRecover;
import com.github.charlemaznable.httpclient.configurer.Configurer;
import io.github.resilience4j.ratelimiter.RateLimiter;

public interface ResilienceRateLimiterConfigurer extends Configurer {

    RateLimiter rateLimiter(String defaultName);

    default <T> ResilienceRateLimiterRecover<T> rateLimiterRecover() {
        return null;
    }
}
