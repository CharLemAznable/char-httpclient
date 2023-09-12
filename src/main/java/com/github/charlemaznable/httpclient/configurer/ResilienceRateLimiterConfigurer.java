package com.github.charlemaznable.httpclient.configurer;

import io.github.resilience4j.ratelimiter.RateLimiter;

public interface ResilienceRateLimiterConfigurer extends Configurer {

    RateLimiter rateLimiter();
}
