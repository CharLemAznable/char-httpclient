package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.common.Mapping.UrlProvider;
import com.github.charlemaznable.httpclient.ohclient.OhFactory.OhLoader;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReloaderTest {

    private static OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());
    private static String baseUrl = "${root}:41270";

    @SneakyThrows
    @Test
    public void testReloader() {
        try (val mockWebServer1 = new MockWebServer();
             val mockWebServer2 = new MockWebServer()) {
            mockWebServer1.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    val requestUrl = checkNotNull(request.getRequestUrl());
                    switch (requestUrl.encodedPath()) {
                        case "/sample":
                            return new MockResponse().setBody("mock server 1");
                        default:
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            });
            mockWebServer1.start(41270);
            mockWebServer2.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    val requestUrl = checkNotNull(request.getRequestUrl());
                    switch (requestUrl.encodedPath()) {
                        case "/sample":
                            return new MockResponse().setBody("mock server 2");
                        default:
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            });
            mockWebServer2.start(41280);

            val httpClient = ohLoader.getClient(ReloadableClient.class);
            val response1 = httpClient.sample();
            assertEquals("mock server 1", response1);

            baseUrl = "${root}:41280";
            httpClient.reload();
            val response2 = httpClient.sample();
            assertEquals("mock server 2", response2);
        }
    }

    @Mapping(urlProvider = UrlReloader.class)
    @OhClient
    public interface ReloadableClient extends Reloadable {

        String sample();
    }

    public static class UrlReloader implements UrlProvider {

        @Override
        public String url(Class<?> clazz) {
            return baseUrl;
        }
    }
}
