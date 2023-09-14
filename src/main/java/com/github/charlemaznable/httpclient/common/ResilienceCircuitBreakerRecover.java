package com.github.charlemaznable.httpclient.common;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

import java.util.function.Function;

public interface ResilienceCircuitBreakerRecover<T> extends Function<CallNotPermittedException, T> {
}
