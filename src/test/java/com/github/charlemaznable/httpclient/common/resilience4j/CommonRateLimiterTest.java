package com.github.charlemaznable.httpclient.common.resilience4j;

import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.ResilienceRateLimiterRecover;
import com.github.charlemaznable.httpclient.configurer.ResilienceRateLimiterConfigurer;
import com.github.charlemaznable.httpclient.configurer.configservice.ResilienceRateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import java.util.concurrent.atomic.AtomicInteger;

import static com.github.charlemaznable.httpclient.common.Utils.dispatcher;
import static java.util.Objects.requireNonNull;

public abstract class CommonRateLimiterTest {

    protected final AtomicInteger countSample = new AtomicInteger();

    protected MockWebServer mockWebServer;

    @SneakyThrows
    protected void startMockWebServer() {
        mockWebServer = new MockWebServer();
        val dispatcher = dispatcher(request -> {
            val requestUrl = requireNonNull(request.getRequestUrl());
            if (requestUrl.encodedPath().equals("/sample")) {
                countSample.incrementAndGet();
                return new MockResponse().setBody("OK");
            }
            return new MockResponse()
                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
        });
        mockWebServer.setDispatcher(dispatcher);
        mockWebServer.start(41420);
    }

    @SneakyThrows
    protected void shutdownMockWebServer() {
        mockWebServer.shutdown();
    }

    public static class DefaultRateLimiterConfig implements ResilienceRateLimiterConfigurer {

        @Override
        public RateLimiter rateLimiter(String defaultName) {
            return RateLimiter.ofDefaults(defaultName);
        }
    }

    public static class CustomRateLimiterConfig implements ResilienceRateLimiterConfig {

        @Override
        public String enabledRateLimiterString() {
            return "true";
        }

        @Override
        public String rateLimiterName() {
            return null;
        }

        @Override
        public String limitForPeriod() {
            return "2";
        }

        @Override
        public String limitRefreshPeriodInNanos() {
            return Long.toString(2000_000_000L); // 2s
        }

        @Override
        public String timeoutDuration() {
            return "0";
        }

        @Override
        public String rateLimiterRecoverString() {
            return "@" + CustomResilienceRateLimiterRecover.class.getName();
        }
    }

    public static class CustomResilienceRateLimiterRecover implements ResilienceRateLimiterRecover<String> {

        @Override
        public String apply(RequestNotPermitted requestNotPermitted) {
            return "OK";
        }
    }

    public static class DisabledRateLimiterConfig implements ResilienceRateLimiterConfig {

        @Override
        public String enabledRateLimiterString() {
            return "false";
        }

        @Override
        public String rateLimiterName() {
            return null;
        }

        @Override
        public String limitForPeriod() {
            return null;
        }

        @Override
        public String limitRefreshPeriodInNanos() {
            return null;
        }

        @Override
        public String timeoutDuration() {
            return null;
        }

        @Override
        public String rateLimiterRecoverString() {
            return null;
        }
    }
}
