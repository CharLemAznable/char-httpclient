package com.github.charlemaznable.httpclient.ohclient.westcache;

import com.github.bingoohuang.westcache.WestCacheable;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.ohclient.OhClient;

import java.util.concurrent.Future;

@Mapping("${root}:41260")
@OhClient
public interface WestCacheClient {

    String sampleNone();

    @WestCacheable
    String sample();

    @WestCacheable
    Future<String> sampleFuture();
}
