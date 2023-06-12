package com.github.charlemaznable.httpclient.wfclient.westcache;

import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.wfclient.WfClient;
import io.smallrye.mutiny.Uni;
import reactor.core.publisher.Mono;

import java.util.concurrent.Future;

@Mapping("${root}:41260")
@WfClient
public interface NoWestCacheClient {

    Future<String> sampleFuture();

    Mono<String> sampleMono();

    rx.Single<String> sampleRx();

    io.reactivex.Single<String> sampleRx2();

    io.reactivex.rxjava3.core.Single<String> sampleRx3();

    Uni<String> sampleUni();
}
