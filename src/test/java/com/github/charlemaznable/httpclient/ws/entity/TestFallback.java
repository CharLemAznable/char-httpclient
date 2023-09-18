package com.github.charlemaznable.httpclient.ws.entity;

import com.github.charlemaznable.httpclient.resilience.function.ResilienceRecover;

public class TestFallback implements ResilienceRecover<String> {

    @Override
    public String apply(Throwable throwable) {
        return null;
    }
}
