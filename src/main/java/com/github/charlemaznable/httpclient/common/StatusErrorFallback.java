package com.github.charlemaznable.httpclient.common;

public final class StatusErrorFallback implements FallbackFunction<Void> {

    @Override
    public Void apply(CommonResponse response) {
        throw new StatusError(response.getStatusCode(), response.getBody());
    }
}
