package com.github.charlemaznable.httpclient.ohclient.westcache;

import com.github.bingoohuang.westcache.WestCacheable;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.ohclient.OhClient;

@Mapping("${root}:41260")
@OhClient
public interface WestCacheClient {

    @WestCacheable
    String sample();
}
