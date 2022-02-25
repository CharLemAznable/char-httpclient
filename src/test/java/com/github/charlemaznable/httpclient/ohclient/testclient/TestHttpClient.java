package com.github.charlemaznable.httpclient.ohclient.testclient;

import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.ohclient.OhClient;

@TestClientMapping
@OhClient
public interface TestHttpClient {

    @Mapping(urlProvider = TestSampleUrlProvider.class)
    String sample();

    default String sampleWrapper() {
        return "{" + sample() + "}";
    }

    @Mapping(urlProvider = TestSampleUrlProviderWrapper.class)
    String sampleWrap();

    @Mapping(urlProvider = TestSampleUrlProviderContext.class)
    String sampleByContext();
}
