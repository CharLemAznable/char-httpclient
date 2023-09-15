package com.github.charlemaznable.httpclient.resilience.configurer.configservice;

public interface CommonResilienceConfig extends ResilienceBulkheadConfig, ResilienceRateLimiterConfig,
        ResilienceCircuitBreakerConfig, ResilienceRetryConfig, ResilienceFallbackConfig {
}
