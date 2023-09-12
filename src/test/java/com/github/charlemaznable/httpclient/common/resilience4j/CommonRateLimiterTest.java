package com.github.charlemaznable.httpclient.common.resilience4j;

import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.configurer.configservice.ResilienceRateLimiterConfig;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.jooq.lambda.fi.lang.CheckedRunnable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.charlemaznable.httpclient.common.Utils.dispatcher;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    protected void checkOptionalException(CheckedRunnable runnable) {
        try {
            runnable.run();
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof RequestNotPermitted);
        } catch (Throwable e) {
            assertTrue(e instanceof RequestNotPermitted);
        }
    }

    public static class CustomRateLimiterConfig implements ResilienceRateLimiterConfig {

        @Override
        public String rateLimiterName() {
            return "DefaultRateLimiter";
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
    }

    public static class DisabledRateLimiterConfig implements ResilienceRateLimiterConfig {

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
    }
}
