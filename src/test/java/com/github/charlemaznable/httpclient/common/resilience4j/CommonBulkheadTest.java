package com.github.charlemaznable.httpclient.common.resilience4j;

import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.configurer.configservice.ResilienceBulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.jooq.lambda.fi.lang.CheckedRunnable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.charlemaznable.core.lang.Await.awaitForSeconds;
import static com.github.charlemaznable.httpclient.common.Utils.dispatcher;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class CommonBulkheadTest {

    protected final AtomicInteger countSample = new AtomicInteger();

    protected MockWebServer mockWebServer;

    @SneakyThrows
    protected void startMockWebServer() {
        mockWebServer = new MockWebServer();
        val dispatcher = dispatcher(request -> {
            val requestUrl = requireNonNull(request.getRequestUrl());
            if (requestUrl.encodedPath().equals("/sample")) {
                countSample.incrementAndGet();
                awaitForSeconds(5);
                return new MockResponse().setBody("OK");
            }
            return new MockResponse()
                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
        });
        mockWebServer.setDispatcher(dispatcher);
        mockWebServer.start(41410);
    }

    @SneakyThrows
    protected void shutdownMockWebServer() {
        mockWebServer.shutdown();
    }

    protected void checkOptionalException(CheckedRunnable runnable) {
        try {
            runnable.run();
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof BulkheadFullException);
        } catch (Throwable e) {
            assertTrue(e instanceof BulkheadFullException);
        }
    }

    public static class CustomBulkheadConfig implements ResilienceBulkheadConfig {

        @Override
        public String bulkheadName() {
            return "DefaultBulkhead";
        }

        @Override
        public String maxConcurrentCalls() {
            return "5";
        }

        @Override
        public String maxWaitDuration() {
            return "10SECONDS";
        }
    }

    public static class DisabledBulkheadConfig implements ResilienceBulkheadConfig {

        @Override
        public String bulkheadName() {
            return null;
        }

        @Override
        public String maxConcurrentCalls() {
            return null;
        }

        @Override
        public String maxWaitDuration() {
            return null;
        }
    }
}
