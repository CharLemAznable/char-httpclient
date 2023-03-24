package com.github.charlemaznable.httpclient.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

@SuppressWarnings("rawtypes")
public interface FallbackFunction<R>
        extends Function<FallbackFunction.Response, R> {

    @Override
    R apply(Response response);

    @AllArgsConstructor
    @Getter
    abstract class Response<T> {

        private int statusCode;
        private T responseBody;

        public abstract String responseBodyAsString();
    }
}
