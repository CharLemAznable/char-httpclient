package com.github.charlemaznable.httpclient.common.testclient2;

import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.testclient.TestClientMapping;
import com.github.charlemaznable.httpclient.ohclient.OhClient;
import com.github.charlemaznable.httpclient.vxclient.VxClient;
import io.vertx.core.Future;

@TestClientMapping
@OhClient
@VxClient
public interface TestHttpClientUnscanned {

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
