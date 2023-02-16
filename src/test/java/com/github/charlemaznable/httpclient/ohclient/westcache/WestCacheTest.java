package com.github.charlemaznable.httpclient.ohclient.westcache;

import com.github.charlemaznable.core.lang.Await;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.annotation.Nonnull;

import static com.github.charlemaznable.core.lang.Str.toStr;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringJUnitConfig(WestCacheConfiguration.class)
public class WestCacheTest {

    @Autowired
    private NoWestCacheClient noWestCacheClient;
    @Autowired
    private WestCacheClient westCacheClient;

    @SneakyThrows
    @Test
    public void testWestCache() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    val requestUrl = requireNonNull(request.getRequestUrl());
                    if ("/sample".equals(requestUrl.encodedPath())) {
                        return new MockResponse().setBody(toStr(System.currentTimeMillis()));
                    }
                    return new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                }
            });
            mockWebServer.start(41260);

            val noCacheSample1 = noWestCacheClient.sample();
            Await.awaitForMillis(100);
            val noCacheSample2 = noWestCacheClient.sample();
            assertNotEquals(noCacheSample1, noCacheSample2);

            val noneCacheSample1 = westCacheClient.sampleNone();
            Await.awaitForMillis(100);
            val noneCacheSample2 = westCacheClient.sampleNone();
            assertNotEquals(noCacheSample1, noCacheSample2);

            val cacheSample1 = westCacheClient.sample();
            Await.awaitForMillis(100);
            val cacheSample2 = westCacheClient.sample();
            assertEquals(cacheSample1, cacheSample2);

            val cacheSampleFuture1 = westCacheClient.sampleFuture();
            Await.awaitForMillis(100);
            val cacheSampleFuture2 = westCacheClient.sampleFuture();
            assertNotSame(cacheSampleFuture1, cacheSampleFuture2);
            val cacheGet1 = cacheSampleFuture1.get();
            val cacheGet2 = cacheSampleFuture2.get();
            assertEquals(cacheGet1, cacheGet2);
        }
    }
}
