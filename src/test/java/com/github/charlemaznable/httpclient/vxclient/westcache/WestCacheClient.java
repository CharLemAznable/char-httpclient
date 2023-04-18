package com.github.charlemaznable.httpclient.vxclient.westcache;

import com.github.bingoohuang.westcache.WestCacheable;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.vxclient.VxClient;
import io.vertx.core.Future;

@Mapping("${root}:41260")
@VxClient
public interface WestCacheClient {

    Future<String> sampleNone();

    rx.Single<String> sampleNoneRx();

    io.reactivex.Single<String> sampleNoneRx2();

    io.reactivex.rxjava3.core.Single<String> sampleNoneRx3();

    @WestCacheable
    Future<String> sample();

    @WestCacheable
    rx.Single<String> sampleRx();

    @WestCacheable
    io.reactivex.Single<String> sampleRx2();

    @WestCacheable
    io.reactivex.rxjava3.core.Single<String> sampleRx3();
}
