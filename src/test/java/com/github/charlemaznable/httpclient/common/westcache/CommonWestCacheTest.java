package com.github.charlemaznable.httpclient.common.westcache;

import com.github.charlemaznable.httpclient.common.HttpStatus;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.charlemaznable.core.lang.Str.toStr;
import static java.util.Objects.requireNonNull;

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
                val body = toStr(counter.incrementAndGet());
                val requestUrl = requireNonNull(request.getRequestUrl());
                if ("/sample500".equalsIgnoreCase(requestUrl.encodedPath())) {
                    return new MockResponse()
                            .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .setBody(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
                } else {
                    return new MockResponse().setBody(body);
                }
            }
        });
        mockWebServer.start(41260);
    }

    @SneakyThrows
    protected void shutdownMockWebServer() {
        mockWebServer.shutdown();
    }
}
