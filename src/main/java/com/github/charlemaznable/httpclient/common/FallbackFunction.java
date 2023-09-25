package com.github.charlemaznable.httpclient.common;

import java.util.function.Function;

public interface FallbackFunction<R>
        extends Function<CommonResponse, R> {

    @Override
    R apply(CommonResponse response);
}
