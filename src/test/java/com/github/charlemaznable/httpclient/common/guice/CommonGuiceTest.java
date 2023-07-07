package com.github.charlemaznable.httpclient.common.guice;

import com.github.charlemaznable.httpclient.common.HttpStatus;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static com.github.charlemaznable.httpclient.common.Utils.dispatcher;
import static java.util.Objects.requireNonNull;

public abstract class CommonGuiceTest {

    protected static final String SAMPLE = "/sample";
    protected static final String SAMPLE_RESULT = "Guice";
    protected static final String SAMPLE_RESULT_WRAP = "{Guice}";
    protected static final String SAMPLE_RESULT_WRAP_I = "[Guice]";
    protected static final String CONTEXT = "/GuiceGuice-SpringSpring-GuiceGuice";
    protected static final String CONTEXT_RESULT = "Done";
    protected static final String SAMPLE_ERROR = "/sampleError";
    protected static final String SAMPLE_ERROR_RESULT = "GuiceError";
    protected static final String SAMPLE_NO_ERROR_RESULT = "GuiceNoError";
    protected static final String SAMPLE_ERROR_RESULT_WRAP_I = "[GuiceError]";

    protected MockWebServer mockWebServer;
    protected MockWebServer mockWebServerError;
    protected MockWebServer mockWebServerNaked;
    protected MockWebServer mockWebServerScan;

    @SneakyThrows
    protected void startMockWebServer() {
        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher(request -> switch (requireNonNull(request.getPath())) {
            case SAMPLE -> new MockResponse().setBody(SAMPLE_RESULT);
            case CONTEXT -> new MockResponse().setBody(CONTEXT_RESULT);
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

    @SneakyThrows
    protected void startMockWebServerError() {
        mockWebServerError = new MockWebServer();
        mockWebServerError.setDispatcher(dispatcher(request -> switch (requireNonNull(request.getPath())) {
            case SAMPLE -> new MockResponse().setBody(SAMPLE_ERROR_RESULT);
            case SAMPLE_ERROR -> new MockResponse().setBody(SAMPLE_RESULT);
            default -> new MockResponse()
                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
        }));
        mockWebServerError.start(41102);
    }

    @SneakyThrows
    protected void shutdownMockWebServerError() {
        mockWebServerError.shutdown();
    }

    @SneakyThrows
    protected void startMockWebServerNaked() {
        mockWebServerNaked = new MockWebServer();
        mockWebServerNaked.setDispatcher(dispatcher(request -> switch (requireNonNull(request.getPath())) {
            case SAMPLE -> new MockResponse().setBody(SAMPLE_ERROR_RESULT);
            case SAMPLE_ERROR -> new MockResponse().setBody(SAMPLE_NO_ERROR_RESULT);
            default -> new MockResponse()
                    .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .setBody(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        }));
        mockWebServerNaked.start(41102);
    }

    @SneakyThrows
    protected void shutdownMockWebServerNaked() {
        mockWebServerNaked.shutdown();
    }

    @SneakyThrows
    protected void startMockWebServerScan() {
        mockWebServerScan = new MockWebServer();
        mockWebServerScan.setDispatcher(dispatcher(request -> new MockResponse().setBody(SAMPLE_RESULT)));
        mockWebServerScan.start(41102);
    }

    @SneakyThrows
    protected void shutdownMockWebServerScan() {
        mockWebServerScan.shutdown();
    }
}
