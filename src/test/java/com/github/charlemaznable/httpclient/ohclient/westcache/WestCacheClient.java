package com.github.charlemaznable.httpclient.ohclient.westcache;

import com.github.bingoohuang.westcache.WestCacheable;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.ohclient.OhClient;

import java.util.concurrent.Future;

@Mapping("${root}:41260")
@OhClient
public interface WestCacheClient {

    String sampleNoneSync();

    Future<String> sampleNone();

    rx.Single<String> sampleNoneRx();

    io.reactivex.Single<String> sampleNoneRx2();

    io.reactivex.rxjava3.core.Single<String> sampleNoneRx3();

    @WestCacheable
    String sampleSync();

    @WestCacheable
    Future<String> sample();

    @WestCacheable
    rx.Single<String> sampleRx();

    @WestCacheable
    io.reactivex.Single<String> sampleRx2();

    @WestCacheable
    io.reactivex.rxjava3.core.Single<String> sampleRx3();
}
