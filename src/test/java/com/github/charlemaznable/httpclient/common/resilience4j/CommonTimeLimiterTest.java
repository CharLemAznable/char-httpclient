package com.github.charlemaznable.httpclient.common.resilience4j;

import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.resilience.configurer.ResilienceTimeLimiterConfigurer;
import com.github.charlemaznable.httpclient.resilience.configurer.configservice.ResilienceTimeLimiterConfig;
import com.github.charlemaznable.httpclient.resilience.function.ResilienceTimeLimiterRecover;
import io.github.resilience4j.timelimiter.TimeLimiter;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import java.util.concurrent.TimeoutException;

import static com.github.charlemaznable.core.lang.Await.awaitForSeconds;
import static com.github.charlemaznable.httpclient.common.Utils.dispatcher;
import static java.util.Objects.requireNonNull;

public abstract class CommonTimeLimiterTest {

    protected MockWebServer mockWebServer;

    @SneakyThrows
    protected void startMockWebServer() {
        mockWebServer = new MockWebServer();
        val dispatcher = dispatcher(request -> {
            val requestUrl = requireNonNull(request.getRequestUrl());
            if (requestUrl.encodedPath().equals("/sample")) {
                awaitForSeconds(3);
                return new MockResponse().setBody("OK");
            }
            return new MockResponse()
                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
        });
        mockWebServer.setDispatcher(dispatcher);
        mockWebServer.start(41450);
    }

    @SneakyThrows
    protected void shutdownMockWebServer() {
        mockWebServer.shutdown();
    }

    public static class DefaultTimeLimiterConfig implements ResilienceTimeLimiterConfigurer {

        @Override
        public TimeLimiter timeLimiter(String defaultName) {
            return TimeLimiter.ofDefaults("DefaultTimeLimiter");
        }
    }

    public static class CustomTimeLimiterConfig implements ResilienceTimeLimiterConfig {

        @Override
        public String enabledTimeLimiterString() {
            return "true";
        }

        @Override
        public String timeLimiterName() {
            return null;
        }

        @Override
        public String timeoutDuration() {
            return null;
        }

        @Override
        public String timeLimiterRecoverString() {
            return "@" + CustomResilienceTimeLimiterRecover.class.getName();
        }
    }

    public static class CustomResilienceTimeLimiterRecover implements ResilienceTimeLimiterRecover<String> {

        @Override
        public String apply(TimeoutException e) {
            return "Timeout";
        }
    }

    public static class DisabledTimeLimiterConfig implements ResilienceTimeLimiterConfig {

        @Override
        public String enabledTimeLimiterString() {
            return "false";
        }

        @Override
        public String timeLimiterName() {
            return null;
        }

        @Override
        public String timeoutDuration() {
            return null;
        }

        @Override
        public String timeLimiterRecoverString() {
            return null;
        }
    }
}
