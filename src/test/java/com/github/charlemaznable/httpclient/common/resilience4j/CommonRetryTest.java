package com.github.charlemaznable.httpclient.common.resilience4j;

import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.configurer.configservice.ResilienceRetryConfig;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import java.util.concurrent.atomic.AtomicInteger;

import static com.github.charlemaznable.httpclient.common.Utils.dispatcher;
import static java.util.Objects.requireNonNull;

public abstract class CommonRetryTest {

    protected final AtomicInteger countSample = new AtomicInteger();

    protected MockWebServer mockWebServer;

    @SneakyThrows
    protected void startMockWebServer() {
        mockWebServer = new MockWebServer();
        val dispatcher = dispatcher(request -> {
            val requestUrl = requireNonNull(request.getRequestUrl());
            if (requestUrl.encodedPath().equals("/sample")) {
                if (countSample.incrementAndGet() == 3) {
                    return new MockResponse().setBody("OK");
                } else {
                    return new MockResponse()
                            .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .setBody(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
                }
            }
            return new MockResponse()
                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
        });
        mockWebServer.setDispatcher(dispatcher);
        mockWebServer.start(41440);
    }

    @SneakyThrows
    protected void shutdownMockWebServer() {
        mockWebServer.shutdown();
    }

    public static class CustomRetryConfig implements ResilienceRetryConfig {

        @Override
        public String retryName() {
            return "DefaultRetry";
        }

        @Override
        public String maxAttempts() {
            return "3";
        }

        @Override
        public String waitDuration() {
            return "100MS";
        }

        @Override
        public String isolatedExecutorString() {
            return "true";
        }
    }

    public static class DisabledRetryConfig implements ResilienceRetryConfig {

        @Override
        public String retryName() {
            return null;
        }

        @Override
        public String maxAttempts() {
            return null;
        }

        @Override
        public String waitDuration() {
            return null;
        }

        @Override
        public String isolatedExecutorString() {
            return null;
        }
    }
}