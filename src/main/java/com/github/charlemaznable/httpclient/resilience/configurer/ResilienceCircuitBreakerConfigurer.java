package com.github.charlemaznable.httpclient.resilience.configurer;

import com.github.charlemaznable.httpclient.resilience.function.ResilienceCircuitBreakerRecover;
import com.github.charlemaznable.httpclient.configurer.Configurer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;

public interface ResilienceCircuitBreakerConfigurer extends Configurer {

    CircuitBreaker circuitBreaker(String defaultName);

    default <T> ResilienceCircuitBreakerRecover<T> circuitBreakerRecover() {
        return null;
    }
}
