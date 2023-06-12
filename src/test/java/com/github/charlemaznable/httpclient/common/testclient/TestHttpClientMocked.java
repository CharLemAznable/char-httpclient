package com.github.charlemaznable.httpclient.common.testclient;

import com.github.charlemaznable.core.testing.mockito.MockitoSpyForTesting;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.ohclient.OhClient;
import com.github.charlemaznable.httpclient.vxclient.VxClient;
import com.github.charlemaznable.httpclient.wfclient.WfClient;
import io.vertx.core.Future;
import reactor.core.publisher.Mono;

@TestClientMapping
@OhClient
@VxClient
@WfClient
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

    @Mapping("/sample")
    Mono<String> wfSample();

    default Mono<String> wfSampleWrapper() {
        return wfSample().map(response -> "[" + response + "]");
    }
}
