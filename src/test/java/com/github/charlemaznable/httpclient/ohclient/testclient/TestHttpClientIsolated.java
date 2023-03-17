package com.github.charlemaznable.httpclient.ohclient.testclient;

import com.github.charlemaznable.core.testing.mockito.MockitoSpyForTesting;
import com.github.charlemaznable.httpclient.ohclient.OhClient;
import com.github.charlemaznable.httpclient.ohclient.annotation.IsolatedConnectionPool;
import com.github.charlemaznable.httpclient.ohclient.annotation.IsolatedDispatcher;

@TestClientMapping
@OhClient
@IsolatedDispatcher
@IsolatedConnectionPool
@MockitoSpyForTesting
public interface TestHttpClientIsolated {

    @IsolatedDispatcher
    @IsolatedConnectionPool
    String sample();

    default String sampleWrapper() {
        return "[" + sample() + "]";
    }
}
