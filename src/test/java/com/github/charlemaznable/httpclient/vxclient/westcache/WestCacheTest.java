package com.github.charlemaznable.httpclient.vxclient.westcache;

import com.github.charlemaznable.httpclient.common.westcache.CommonWestCacheTest;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringJUnitConfig(WestCacheConfiguration.class)
public class WestCacheTest extends CommonWestCacheTest {

    @Autowired
    private NoWestCacheClient noWestCacheClient;
    @Autowired
    private WestCacheClient westCacheClient;

    @Test
    public void testWestCache() {
        startMockWebServer();

        val noWestCache = new RespResult();
        noWestCacheClient.sample().onSuccess(noWestCache::setResult1);
        noWestCacheClient.sample().onSuccess(noWestCache::setResult2);
        await().forever().untilAsserted(() ->
                assertNotEquals(noWestCache.getResult1(), noWestCache.getResult2()));

        val noneCache = new RespResult();
        westCacheClient.sampleNone().onSuccess(noneCache::setResult1);
        westCacheClient.sampleNone().onSuccess(noneCache::setResult2);
        await().forever().untilAsserted(() ->
                assertNotEquals(noneCache.getResult1(), noneCache.getResult2()));

        val cache = new RespResult();
        val cacheSample1 = westCacheClient.sample();
        val cacheSample2 = westCacheClient.sample();
        assertNotSame(cacheSample1, cacheSample2);
        cacheSample1.onSuccess(cache::setResult1);
        cacheSample1.onSuccess(cache::setResult2);
        await().forever().untilAsserted(() ->
                assertEquals(cache.getResult1(), cache.getResult2()));

        shutdownMockWebServer();
    }

    @Getter
    @Setter
    private static final class RespResult {
        private String result1;
        private String result2;
    }
}
