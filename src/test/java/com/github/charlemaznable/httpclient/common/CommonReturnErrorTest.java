package com.github.charlemaznable.httpclient.common;

import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.codec.Json.jsonOf;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.httpclient.common.Utils.dispatcher;
import static java.util.Objects.requireNonNull;

public abstract class CommonReturnErrorTest {

    protected MockWebServer mockWebServer;

    @SneakyThrows
    protected void startMockWebServer() {
        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher(request -> switch (requireNonNull(request.getPath())) {
            case "/sampleFuture", "/sampleList" -> new MockResponse()
                    .setResponseCode(HttpStatus.OK.value())
                    .setBody(json(newArrayList("John", "Doe")));
            case "/sampleMapError" -> new MockResponse()
                    .setResponseCode(HttpStatus.OK.value())
                    .setBody("John Doe");
            case "/sampleMap", "/samplePair", "/sampleTriple" -> new MockResponse()
                    .setResponseCode(HttpStatus.OK.value())
                    .setBody(jsonOf("John", "Doe"));
            default -> new MockResponse()
                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
        }));
        mockWebServer.start(41196);
    }

    @SneakyThrows
    protected void shutdownMockWebServer() {
        mockWebServer.shutdown();
    }
}
