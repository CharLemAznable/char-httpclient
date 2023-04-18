package com.github.charlemaznable.httpclient.vxclient.westcache;

import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.vxclient.VxClient;
import io.vertx.core.Future;

@Mapping("${root}:41260")
@VxClient
public interface NoWestCacheClient {

    Future<String> sample();

    rx.Single<String> sampleRx();

    io.reactivex.Single<String> sampleRx2();

    io.reactivex.rxjava3.core.Single<String> sampleRx3();
}
