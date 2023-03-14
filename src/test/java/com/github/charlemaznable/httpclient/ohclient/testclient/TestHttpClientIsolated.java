package com.github.charlemaznable.httpclient.ohclient.testclient;

import com.github.charlemaznable.core.testing.mockito.MockitoSpyForTesting;
import com.github.charlemaznable.httpclient.ohclient.OhClient;
import com.github.charlemaznable.httpclient.ohclient.annotation.IsolatedConnectionPool;

@TestClientMapping
@OhClient
@IsolatedConnectionPool
@MockitoSpyForTesting
public interface TestHttpClientIsolated {

    @IsolatedConnectionPool
    String sample();

    default String sampleWrapper() {
        return "[" + sample() + "]";
    }
}
