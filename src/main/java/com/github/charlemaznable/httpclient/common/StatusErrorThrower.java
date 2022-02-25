package com.github.charlemaznable.httpclient.common;

public final class StatusErrorThrower
        implements FallbackFunction<Void> {

    @Override
    public Void apply(Response response) {
        throw new StatusError(response.getStatusCode(),
                response.responseBodyAsString());
    }
}
