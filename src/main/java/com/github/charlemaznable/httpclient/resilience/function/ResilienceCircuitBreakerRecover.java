package com.github.charlemaznable.httpclient.resilience.function;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

import java.util.function.Function;

public interface ResilienceCircuitBreakerRecover<T> extends Function<CallNotPermittedException, T> {
}
