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
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = WestCacheConfiguration.class)
public class WestCacheTest {

    @Autowired
    private NoWestCacheClient noWestCacheClient;
    @Autowired
    private WestCacheClient westCacheClient;

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

            val noCacheSample1 = noWestCacheClient.sample();
            Await.awaitForMillis(100);
            val noCacheSample2 = noWestCacheClient.sample();
            assertNotEquals(noCacheSample1, noCacheSample2);

            val cacheSample1 = westCacheClient.sample();
            Await.awaitForMillis(100);
            val cacheSample2 = westCacheClient.sample();
            assertEquals(cacheSample1, cacheSample2);
        }
    }
}
