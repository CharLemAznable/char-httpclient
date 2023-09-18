package com.github.charlemaznable.httpclient.resilience.function;

import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public interface ResilienceTimeLimiterRecover<T> extends Function<TimeoutException, T> {
}
