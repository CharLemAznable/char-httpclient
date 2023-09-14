package com.github.charlemaznable.httpclient.common;

import io.github.resilience4j.bulkhead.BulkheadFullException;

import java.util.function.Function;

public interface ResilienceBulkheadRecover<T> extends Function<BulkheadFullException, T> {
}
