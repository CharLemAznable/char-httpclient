package com.github.charlemaznable.httpclient.configurer;

import com.github.charlemaznable.httpclient.common.ResilienceCircuitBreakerRecover;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;

public interface ResilienceCircuitBreakerConfigurer extends Configurer {

    CircuitBreaker circuitBreaker(String defaultName);

    default <T> ResilienceCircuitBreakerRecover<T> circuitBreakerRecover() {
        return null;
    }
}
