package com.github.charlemaznable.httpclient.ohclient.westcache;

import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.ohclient.OhClient;
import io.smallrye.mutiny.Uni;

import java.util.concurrent.Future;

@Mapping("${root}:41260")
@OhClient
public interface NoWestCacheClient {

    String sampleSync();

    Future<String> sample();

    rx.Single<String> sampleRx();

    io.reactivex.Single<String> sampleRx2();

    io.reactivex.rxjava3.core.Single<String> sampleRx3();

    Uni<String> sampleUni();
}
