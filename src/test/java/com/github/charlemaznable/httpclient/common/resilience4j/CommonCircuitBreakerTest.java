package com.github.charlemaznable.httpclient.common.resilience4j;

import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.configurer.configservice.ResilienceCircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.jooq.lambda.fi.lang.CheckedRunnable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.charlemaznable.httpclient.common.Utils.dispatcher;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class CommonCircuitBreakerTest {

    protected final AtomicBoolean errorState = new AtomicBoolean(true);
    protected final AtomicInteger countSample = new AtomicInteger();

    protected MockWebServer mockWebServer;

    @SneakyThrows
    protected void startMockWebServer() {
        mockWebServer = new MockWebServer();
        val dispatcher = dispatcher(request -> {
            val requestUrl = requireNonNull(request.getRequestUrl());
            if (requestUrl.encodedPath().equals("/sample")) {
                if (countSample.incrementAndGet() % 2 == 0 && errorState.get()) {
                    return new MockResponse()
                            .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .setBody(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
                }
                return new MockResponse().setBody("OK");
            }
            return new MockResponse()
                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
        });
        mockWebServer.setDispatcher(dispatcher);
        mockWebServer.start(41430);
    }

    @SneakyThrows
    protected void shutdownMockWebServer() {
        mockWebServer.shutdown();
    }

    protected Runnable runQuietly(CheckedRunnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Throwable e) {
                // ignored
            }
        };
    }

    protected Runnable checkOptionalException(CheckedRunnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (ExecutionException e) {
                assertTrue(e.getCause() instanceof CallNotPermittedException);
            } catch (Throwable e) {
                assertTrue(e instanceof CallNotPermittedException);
            }
        };
    }

    public static class CustomCircuitBreakerConfig implements ResilienceCircuitBreakerConfig {

        @Override
        public String circuitBreakerName() {
            return "DefaultCircuitBreaker";
        }

        @Override
        public String slidingWindowType() {
            return null;
        }

        @Override
        public String slidingWindowSize() {
            return "10";
        }

        @Override
        public String minimumNumberOfCalls() {
            return "10";
        }

        @Override
        public String failureRateThreshold() {
            return null;
        }

        @Override
        public String slowCallRateThreshold() {
            return null;
        }

        @Override
        public String slowCallDurationThreshold() {
            return null;
        }

        @Override
        public String automaticTransitionFromOpenToHalfOpenEnabled() {
            return null;
        }

        @Override
        public String waitDurationInOpenState() {
            return "10S";
        }

        @Override
        public String permittedNumberOfCallsInHalfOpenState() {
            return "5";
        }

        @Override
        public String maxWaitDurationInHalfOpenState() {
            return null;
        }
    }

    public static class DisabledCircuitBreakerConfig implements ResilienceCircuitBreakerConfig {

        @Override
        public String circuitBreakerName() {
            return null;
        }

        @Override
        public String slidingWindowType() {
            return null;
        }

        @Override
        public String slidingWindowSize() {
            return null;
        }

        @Override
        public String minimumNumberOfCalls() {
            return null;
        }

        @Override
        public String failureRateThreshold() {
            return null;
        }

        @Override
        public String slowCallRateThreshold() {
            return null;
        }

        @Override
        public String slowCallDurationThreshold() {
            return null;
        }

        @Override
        public String automaticTransitionFromOpenToHalfOpenEnabled() {
            return null;
        }

        @Override
        public String waitDurationInOpenState() {
            return null;
        }

        @Override
        public String permittedNumberOfCallsInHalfOpenState() {
            return null;
        }

        @Override
        public String maxWaitDurationInHalfOpenState() {
            return null;
        }
    }
}
