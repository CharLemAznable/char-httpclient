package com.github.charlemaznable.httpclient.common.spring;

import com.github.charlemaznable.httpclient.common.HttpStatus;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static com.github.charlemaznable.httpclient.common.Utils.dispatcher;
import static java.util.Objects.requireNonNull;

public abstract class CommonSpringTest {

    protected static final String SAMPLE = "Sample";

    protected MockWebServer mockWebServer;

    @SneakyThrows
    protected void startMockWebServer() {
        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher(request -> switch (requireNonNull(request.getPath())) {
            case "/sample" -> new MockResponse().setBody(SAMPLE);
            case "/SpringSpring-SpringSpring-GuiceGuice" -> new MockResponse().setBody("Done");
            default -> new MockResponse()
                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
        }));
        mockWebServer.start(41102);
    }

    @SneakyThrows
    protected void shutdownMockWebServer() {
        mockWebServer.shutdown();
    }
}
