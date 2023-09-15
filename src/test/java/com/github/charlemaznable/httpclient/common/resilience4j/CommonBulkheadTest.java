package com.github.charlemaznable.httpclient.common.resilience4j;

import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.resilience.configurer.ResilienceBulkheadConfigurer;
import com.github.charlemaznable.httpclient.resilience.configurer.configservice.ResilienceBulkheadConfig;
import com.github.charlemaznable.httpclient.resilience.function.ResilienceBulkheadRecover;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import java.util.concurrent.atomic.AtomicInteger;

import static com.github.charlemaznable.core.lang.Await.awaitForSeconds;
import static com.github.charlemaznable.httpclient.common.Utils.dispatcher;
import static java.util.Objects.requireNonNull;

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

    public static class DefaultBulkheadConfig implements ResilienceBulkheadConfigurer {

        @Override
        public Bulkhead bulkhead(String defaultName) {
            return Bulkhead.ofDefaults(defaultName);
        }
    }

    public static class CustomBulkheadConfig implements ResilienceBulkheadConfig {

        @Override
        public String enabledBulkheadString() {
            return "true";
        }

        @Override
        public String bulkheadName() {
            return null;
        }

        @Override
        public String maxConcurrentCalls() {
            return "5";
        }

        @Override
        public String maxWaitDuration() {
            return "10SECONDS";
        }

        @Override
        public String bulkheadRecoverString() {
            return "@" + CustomResilienceBulkheadRecover.class.getName();
        }
    }

    public static class CustomResilienceBulkheadRecover implements ResilienceBulkheadRecover<String> {

        @Override
        public String apply(BulkheadFullException e) {
            return "OK";
        }
    }

    public static class DisabledBulkheadConfig implements ResilienceBulkheadConfig {

        @Override
        public String enabledBulkheadString() {
            return "false";
        }

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

        @Override
        public String bulkheadRecoverString() {
            return null;
        }
    }
}
