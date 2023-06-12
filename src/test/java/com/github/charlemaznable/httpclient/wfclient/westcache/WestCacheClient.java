package com.github.charlemaznable.httpclient.wfclient.westcache;

import com.github.bingoohuang.westcache.WestCacheable;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.wfclient.WfClient;
import io.smallrye.mutiny.Uni;
import reactor.core.publisher.Mono;

import java.util.concurrent.Future;

@Mapping("${root}:41260")
@WfClient
public interface WestCacheClient {

    Future<String> sampleNoneJava();

    Mono<String> sampleNoneMono();

    rx.Single<String> sampleNoneRx();

    io.reactivex.Single<String> sampleNoneRx2();

    io.reactivex.rxjava3.core.Single<String> sampleNoneRx3();

    Uni<String> sampleNoneUni();

    @WestCacheable
    Future<String> sampleJava();

    @WestCacheable
    Mono<String> sampleMono();

    @WestCacheable
    rx.Single<String> sampleRx();

    @WestCacheable
    io.reactivex.Single<String> sampleRx2();

    @WestCacheable
    io.reactivex.rxjava3.core.Single<String> sampleRx3();

    @WestCacheable
    Uni<String> sampleUni();

    @WestCacheable
    Uni<String> sample500();
}
