package com.github.charlemaznable.httpclient.configurer;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;

public interface ResilienceCircuitBreakerConfigurer extends Configurer {

    CircuitBreaker circuitBreaker();
}
