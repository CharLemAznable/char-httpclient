package com.github.charlemaznable.httpclient.ohclient.testclient;

import com.github.charlemaznable.httpclient.ohclient.OhClient;
import com.github.charlemaznable.httpclient.ohclient.annotation.IsolatedConnectionPool;

@TestClientMapping
@OhClient
@IsolatedConnectionPool
public interface TestHttpClientIsolated {

    @IsolatedConnectionPool
    String sample();

    default String sampleWrapper() {
        return "[" + sample() + "]";
    }
}
