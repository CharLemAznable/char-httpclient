package com.github.charlemaznable.httpclient.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import javax.annotation.Nonnull;

import static com.github.charlemaznable.core.codec.Json.json;
import static java.util.Objects.requireNonNull;

public abstract class CommonReturnTripleTest {

    protected MockWebServer mockWebServer;

    @SneakyThrows
    protected void startMockWebServer() {
        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Nonnull
            @Override
            public MockResponse dispatch(@Nonnull RecordedRequest request) {
                return switch (requireNonNull(request.getPath())) {
                    case "/sampleStatusCodeAndBean", "/sampleFutureStatusCodeAndBean" -> new MockResponse()
                            .setResponseCode(HttpStatus.OK.value())
                            .setBody(json(new Bean("John")));
                    case "/sampleRawStreamAndBean", "/sampleFutureRawStreamAndBean" -> new MockResponse()
                            .setResponseCode(HttpStatus.OK.value())
                            .setBody(json(new Bean("Doe")));
                    default -> new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                };
            }
        });
        mockWebServer.start(41195);
    }

    @SneakyThrows
    protected void shutdownMockWebServer() {
        mockWebServer.shutdown();
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class Bean {

        private String name;
    }
}
