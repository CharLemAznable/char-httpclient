package com.github.charlemaznable.httpclient.ohclient.westcache;

import com.github.bingoohuang.westcache.WestCacheable;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.ohclient.OhClient;

import java.util.concurrent.Future;

@Mapping("${root}:41260")
@OhClient
public interface WestCacheClient {

    @Mapping("/sample")
    String sampleNone();

    @WestCacheable
    String sample();

    @Mapping("/sample")
    @WestCacheable
    Future<String> sampleFuture();
}
