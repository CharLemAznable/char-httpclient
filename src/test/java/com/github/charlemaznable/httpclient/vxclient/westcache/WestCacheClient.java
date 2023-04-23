package com.github.charlemaznable.httpclient.vxclient.westcache;

import com.github.bingoohuang.westcache.WestCacheable;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.vxclient.VxClient;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Future;
import reactor.core.publisher.Mono;

@Mapping("${root}:41260")
@VxClient
public interface WestCacheClient {

    Future<String> sampleNone();

    java.util.concurrent.Future<String> sampleNoneJava();

    Mono<String> sampleNoneMono();

    rx.Single<String> sampleNoneRx();

    io.reactivex.Single<String> sampleNoneRx2();

    io.reactivex.rxjava3.core.Single<String> sampleNoneRx3();

    Uni<String> sampleNoneUni();

    @WestCacheable
    Future<String> sample();

    @WestCacheable
    java.util.concurrent.Future<String> sampleJava();

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
}
