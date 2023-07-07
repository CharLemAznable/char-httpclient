package com.github.charlemaznable.httpclient.common;

import com.github.charlemaznable.httpclient.ohclient.CncTest;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.httpclient.common.Utils.dispatcher;

public abstract class CommonCncTest {

    protected static final String CONTENT = "content";

    protected MockWebServer mockWebServer;

    @SneakyThrows
    protected void startMockWebServer() {
        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher(request -> {
            val testResponse = new CncTest.TestResponse();
            testResponse.setContent(CONTENT);
            return new MockResponse().setBody(json(testResponse));
        }));
        mockWebServer.start(41200);
    }

    @SneakyThrows
    protected void shutdownMockWebServer() {
        mockWebServer.shutdown();
    }

    public interface OtherRequest<T extends CncTest.OtherResponse> {

        Class<T> getResponseClass();
    }

    public interface OtherResponse {
    }

    public static class TestRequest implements CncRequest<CncTest.TestResponse> {

        @Override
        public Class<? extends CncTest.TestResponse> responseClass() {
            return CncTest.TestResponse.class;
        }
    }

    public static class TestResponse implements CncResponse {

        @Getter
        @Setter
        private String content;
    }
}
