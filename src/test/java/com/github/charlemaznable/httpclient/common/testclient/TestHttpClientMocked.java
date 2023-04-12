package com.github.charlemaznable.httpclient.common.testclient;

import com.github.charlemaznable.core.testing.mockito.MockitoSpyForTesting;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.ohclient.OhClient;
import com.github.charlemaznable.httpclient.vxclient.VxClient;
import io.vertx.core.Future;

@TestClientMapping
@OhClient
@VxClient
@MockitoSpyForTesting
public interface TestHttpClientMocked {

    @Mapping("/sample")
    String ohSample();

    default String ohSampleWrapper() {
        return "[" + ohSample() + "]";
    }

    @Mapping("/sample")
    Future<String> vxSample();

    default Future<String> vxSampleWrapper() {
        return vxSample().map(response -> "[" + response + "]");
    }
}
