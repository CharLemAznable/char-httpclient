package com.github.charlemaznable.httpclient.common;

import com.github.charlemaznable.httpclient.annotation.ExtraUrlQuery;
import com.github.charlemaznable.httpclient.configurer.ExtraUrlQueryConfigurer;
import com.github.charlemaznable.httpclient.configurer.ExtraUrlQueryDisabledConfigurer;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import javax.annotation.Nonnull;
import java.util.Map;

import static com.github.charlemaznable.core.codec.Json.unJson;
import static com.github.charlemaznable.httpclient.common.Utils.dispatcher;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class CommonExtraQueryTest {

    protected MockWebServer mockWebServer;

    @SneakyThrows
    protected void startMockWebServer() {
        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher(request -> {
            val requestUrl = requireNonNull(request.getRequestUrl());
            switch (requestUrl.encodedPath()) {
                case "/sampleGet" -> {
                    assertEquals("GET", request.getMethod());
                    assertEquals("EQV1", requestUrl.queryParameter("EQ1"));
                    assertEquals("PV1", requestUrl.queryParameter("P1"));
                    return new MockResponse().setBody("OK");
                }
                case "/samplePost" -> {
                    assertEquals("POST", request.getMethod());
                    assertEquals("EQV2", requestUrl.queryParameter("EQ1"));
                    val body = unJson(request.getBody().readUtf8());
                    assertEquals("PV1", body.get("P1"));
                    return new MockResponse().setBody("OK");
                }
                case "/sampleNone" -> {
                    assertEquals("GET", request.getMethod());
                    assertNull(requestUrl.queryParameter("EQ1"));
                    assertEquals("PV1", requestUrl.queryParameter("P1"));
                    return new MockResponse().setBody("OK");
                }
                default -> {
                    return new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                }
            }
        }));
        mockWebServer.start(41230);
    }

    @SneakyThrows
    protected void shutdownMockWebServer() {
        mockWebServer.shutdown();
    }

    public static class ExtraOnClass implements ExtraUrlQuery.ExtraUrlQueryBuilder {

        @Override
        public String build(@Nonnull Map<String, Object> parameterMap,
                            @Nonnull Map<String, Object> contextMap) {
            return "EQ1=EQV1";
        }
    }

    public static class ExtraOnMethod implements ExtraUrlQuery.ExtraUrlQueryBuilder {

        @Override
        public String build(@Nonnull Map<String, Object> parameterMap,
                            @Nonnull Map<String, Object> contextMap) {
            return "EQ1=EQV2";
        }
    }

    public static class ExtraOnClassConfig implements ExtraUrlQueryConfigurer {

        @Override
        public ExtraUrlQuery.ExtraUrlQueryBuilder extraUrlQueryBuilder() {
            return new ExtraOnClass();
        }
    }

    public static class ExtraOnMethodConfig implements ExtraUrlQueryConfigurer {

        @Override
        public ExtraUrlQuery.ExtraUrlQueryBuilder extraUrlQueryBuilder() {
            return new ExtraOnMethod();
        }
    }

    public static class ExtraNoneConfig implements ExtraUrlQueryDisabledConfigurer {
    }
}
