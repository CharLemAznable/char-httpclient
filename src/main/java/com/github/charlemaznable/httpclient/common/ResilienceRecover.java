package com.github.charlemaznable.httpclient.common;

import java.util.function.Function;

public interface ResilienceRecover<T> extends Function<Throwable, T> {
}
