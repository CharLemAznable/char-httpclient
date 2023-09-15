package com.github.charlemaznable.httpclient.resilience.function;

import java.util.function.Function;

public interface ResilienceRecover<T> extends Function<Throwable, T> {
}
