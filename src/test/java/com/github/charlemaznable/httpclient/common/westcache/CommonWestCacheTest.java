package com.github.charlemaznable.httpclient.common.westcache;

import lombok.SneakyThrows;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.charlemaznable.core.lang.Str.toStr;

public abstract class CommonWestCacheTest {

    protected MockWebServer mockWebServer;
    protected AtomicInteger counter = new AtomicInteger();

    @SneakyThrows
    protected void startMockWebServer() {
        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Nonnull
            @Override
            public MockResponse dispatch(@Nonnull RecordedRequest request) {
                return new MockResponse().setBody(toStr(counter.incrementAndGet()));
            }
        });
        mockWebServer.start(41260);
    }

    @SneakyThrows
    protected void shutdownMockWebServer() {
        mockWebServer.shutdown();
    }
}
