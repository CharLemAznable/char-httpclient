package com.github.charlemaznable.httpclient.resilience.function;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;

import java.util.function.Function;

public interface ResilienceRateLimiterRecover<T> extends Function<RequestNotPermitted, T> {
}
