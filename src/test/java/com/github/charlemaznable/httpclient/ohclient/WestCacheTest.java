package com.github.charlemaznable.httpclient.ohclient;

import com.github.bingoohuang.westcache.WestCacheable;
import com.github.charlemaznable.core.lang.Await;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.ohclient.OhFactory.OhLoader;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class WestCacheTest {

    private static OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @SneakyThrows
    @Test
    public void testWestCache() {
        try (val mockWebServer1 = new MockWebServer()) {
            mockWebServer1.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    val requestUrl = checkNotNull(request.getRequestUrl());
                    switch (requestUrl.encodedPath()) {
                        case "/sample":
                            return new MockResponse().setBody(toStr(System.currentTimeMillis()));
                        default:
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            });
            mockWebServer1.start(41260);

            val noWestCacheClient = ohLoader.getClient(NoWestCacheClient.class);
            val noCacheSample1 = noWestCacheClient.sample();
            Await.awaitForMillis(100);
            val noCacheSample2 = noWestCacheClient.sample();
            assertNotEquals(noCacheSample1, noCacheSample2);

            val westCacheClient = ohLoader.getClient(WestCacheClient.class);
            val cacheSample1 = westCacheClient.sample();
            Await.awaitForMillis(100);
            val cacheSample2 = westCacheClient.sample();
            assertEquals(cacheSample1, cacheSample2);
        }
    }

    @Mapping("${root}:41260")
    @OhClient
    public interface NoWestCacheClient {

        String sample();
    }

    @Mapping("${root}:41260")
    @OhClient
    public interface WestCacheClient {

        @WestCacheable
        String sample();
    }
}
