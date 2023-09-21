package com.github.charlemaznable.httpclient.common.resilience4j;

import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.StatusError;
import com.github.charlemaznable.httpclient.resilience.configurer.ResilienceFallbackConfigurer;
import com.github.charlemaznable.httpclient.resilience.configurer.ResilienceRetryConfigurer;
import com.github.charlemaznable.httpclient.resilience.configurer.configservice.ResilienceFallbackConfig;
import com.github.charlemaznable.httpclient.resilience.configurer.configservice.ResilienceRetryConfig;
import com.github.charlemaznable.httpclient.resilience.function.ResilienceRecover;
import io.github.resilience4j.retry.MaxRetriesExceededException;
import io.github.resilience4j.retry.Retry;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static com.github.charlemaznable.httpclient.common.Utils.dispatcher;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
            } else if (requestUrl.encodedPath().equals("/sample2")) {
                if (countSample.incrementAndGet() == 3) {
                    return new MockResponse().setBody("OK");
                } else {
                    return new MockResponse().setBody("ERROR");
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

    public static class DefaultRetryConfig
            implements ResilienceRetryConfigurer, ResilienceFallbackConfigurer {

        @Override
        public Retry retry(String defaultName) {
            return Retry.ofDefaults(defaultName);
        }
    }

    public static class CustomRetryConfig implements ResilienceRetryConfig {

        @Override
        public String enabledRetryString() {
            return "true";
        }

        @Override
        public String retryName() {
            return null;
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
        public String retryOnResultPredicate() {
            return "@" + CustomResultPredicate.class.getName();
        }

        @Override
        public String failAfterMaxAttempts() {
            return "true";
        }

        @Override
        public String isolatedExecutorString() {
            return "true";
        }
    }

    public static class CustomResultPredicate implements Predicate<String> {

        @Override
        public boolean test(String s) {
            return !"OK".equals(s);
        }
    }

    public static class CustomFallbackConfig implements ResilienceFallbackConfig {

        @Override
        public String recoverString() {
            return "@" + CustomResilienceRecover.class.getName();
        }
    }

    public static class CustomResilienceRecover implements ResilienceRecover<String> {

        @Override
        public String apply(Throwable throwable) {
            assertTrue(throwable instanceof StatusError);
            return "NotOK";
        }
    }

    public static class CustomResilienceRecover2 implements ResilienceRecover<String> {

        @Override
        public String apply(Throwable throwable) {
            assertTrue(throwable instanceof MaxRetriesExceededException);
            return "NotOK2";
        }
    }

    public static class DisabledRetryConfig implements ResilienceRetryConfig {

        @Override
        public String enabledRetryString() {
            return "false";
        }

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
        public String retryOnResultPredicate() {
            return null;
        }

        @Override
        public String failAfterMaxAttempts() {
            return null;
        }

        @Override
        public String isolatedExecutorString() {
            return null;
        }
    }
}
