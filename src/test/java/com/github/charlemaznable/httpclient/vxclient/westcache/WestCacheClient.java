package com.github.charlemaznable.httpclient.vxclient.westcache;

import com.github.bingoohuang.westcache.WestCacheable;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.vxclient.VxClient;
import io.vertx.core.Future;

@Mapping("${root}:41260")
@VxClient
public interface WestCacheClient {

    Future<String> sampleNone();

    @WestCacheable
    Future<String> sample();
}
