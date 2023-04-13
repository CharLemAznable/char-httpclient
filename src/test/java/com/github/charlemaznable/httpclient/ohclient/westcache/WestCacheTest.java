package com.github.charlemaznable.httpclient.ohclient.westcache;

import com.github.charlemaznable.httpclient.common.westcache.CommonWestCacheTest;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

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

    @SneakyThrows
    @Test
    public void testWestCache() {
        startMockWebServer();

        val noCacheSample1 = noWestCacheClient.sample();
        val noCacheSample2 = noWestCacheClient.sample();
        assertNotEquals(noCacheSample1, noCacheSample2);

        val noneCacheSample1 = westCacheClient.sampleNone();
        val noneCacheSample2 = westCacheClient.sampleNone();
        assertNotEquals(noCacheSample1, noCacheSample2);

        val cacheSample1 = westCacheClient.sample();
        val cacheSample2 = westCacheClient.sample();
        assertEquals(cacheSample1, cacheSample2);

        val cacheSampleFuture1 = westCacheClient.sampleFuture();
        val cacheSampleFuture2 = westCacheClient.sampleFuture();
        assertNotSame(cacheSampleFuture1, cacheSampleFuture2);
        val cacheGet1 = cacheSampleFuture1.get();
        val cacheGet2 = cacheSampleFuture2.get();
        assertEquals(cacheGet1, cacheGet2);

        shutdownMockWebServer();
    }
}
