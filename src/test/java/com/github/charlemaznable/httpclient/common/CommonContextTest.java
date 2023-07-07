package com.github.charlemaznable.httpclient.common;

import com.github.charlemaznable.core.lang.Mapp;
import com.github.charlemaznable.httpclient.annotation.ContentFormat;
import com.github.charlemaznable.httpclient.annotation.RequestExtend;
import com.github.charlemaznable.httpclient.annotation.ResponseParse;
import com.github.charlemaznable.httpclient.configurer.ContentFormatConfigurer;
import com.github.charlemaznable.httpclient.configurer.FixedContextsConfigurer;
import com.github.charlemaznable.httpclient.configurer.MappingConfigurer;
import com.github.charlemaznable.httpclient.configurer.RequestExtendConfigurer;
import com.github.charlemaznable.httpclient.configurer.RequestExtendDisabledConfigurer;
import com.github.charlemaznable.httpclient.configurer.RequestMethodConfigurer;
import com.github.charlemaznable.httpclient.configurer.ResponseParseConfigurer;
import com.github.charlemaznable.httpclient.configurer.ResponseParseDisabledConfigurer;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.codec.Json.unJson;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static com.github.charlemaznable.httpclient.common.Utils.dispatcher;
import static com.google.common.net.MediaType.JSON_UTF_8;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class CommonContextTest {

    protected MockWebServer mockWebServer;

    @SneakyThrows
    protected void startMockWebServer() {
        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher(request -> {
            val body = unJson(request.getBody().readUtf8());
            switch (requireNonNull(request.getPath())) {
                case "/sampleDefault":
                    assertEquals("EH1", request.getHeader("H1"));
                    assertEquals("CV1", body.get("C1"));
                    assertEquals("CV2", body.get("C2"));
                    assertNull(body.get("C3"));
                    assertNull(body.get("C4"));
                    return new MockResponse().setBody("OK");
                case "/sampleMapping":
                    assertEquals("EH1", request.getHeader("H1"));
                    assertEquals("CV1", body.get("C1"));
                    assertNull(body.get("C2"));
                    assertEquals("CV3", body.get("C3"));
                    assertNull(body.get("C4"));
                    return new MockResponse().setBody("OK");
                case "/sampleContexts":
                    assertEquals("EH1", request.getHeader("H1"));
                    assertEquals("CV1", body.get("C1"));
                    assertNull(body.get("C2"));
                    assertNull(body.get("C3"));
                    assertEquals("CV4", body.get("C4"));
                    return new MockResponse().setBody("OK");
                case "/sampleNone":
                    assertNull(request.getHeader("H1"));
                    assertEquals("CV1", body.get("C1"));
                    assertEquals("CV2", body.get("C2"));
                    assertNull(body.get("C3"));
                    assertNull(body.get("C4"));
                    return new MockResponse().setBody("OK");
                default:
                    return new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
            }
        }));
        mockWebServer.start(41170);
    }

    @SneakyThrows
    protected void shutdownMockWebServer() {
        mockWebServer.shutdown();
    }

    public static class TestContextFormatter implements ContentFormat.ContentFormatter {

        private final Map<String, String> contextValue = Mapp.of(
                "V1", "CV1",
                "V2", "CV2",
                "V3", "CV3",
                "V4", "CV4"
        );

        @Override
        public String contentType() {
            return JSON_UTF_8.toString();
        }

        @Override
        public String format(@Nonnull Map<String, Object> parameterMap,
                             @Nonnull Map<String, Object> contextMap) {
            Map<String, String> content = newHashMap();
            for (val contextEntry : contextMap.entrySet()) {
                content.put(contextEntry.getKey(), contextValue
                        .get(toStr(contextEntry.getValue())));
            }
            return json(content);
        }
    }

    public static class TestResponseParser implements ResponseParse.ResponseParser {

        @Override
        public Object parse(@Nonnull String responseContent,
                            @Nonnull Class<?> returnType,
                            @Nonnull Map<String, Object> contextMap) {
            assertEquals("OK", responseContent);
            assertEquals(TestResponse.class, returnType);
            val testResponse = new TestResponse();
            testResponse.setResponse(responseContent);
            return testResponse;
        }
    }

    @Getter
    @Setter
    public static class TestResponse {

        private String response;
    }

    public static class TestRequestExtender implements RequestExtend.RequestExtender {

        @Override
        public void extend(List<Pair<String, String>> headers,
                           List<Pair<String, String>> pathVars,
                           List<Pair<String, Object>> parameters,
                           List<Pair<String, Object>> contexts) {
            headers.add(Pair.of("H1", "EH1"));
        }
    }

    public static class ContextHttpClientConfig implements MappingConfigurer, RequestMethodConfigurer,
            ContentFormatConfigurer, ResponseParseConfigurer, RequestExtendConfigurer, FixedContextsConfigurer {

        @Override
        public List<String> urls() {
            return newArrayList("${root}:41170");
        }

        @Override
        public HttpMethod requestMethod() {
            return HttpMethod.POST;
        }

        @Override
        public ContentFormat.ContentFormatter contentFormatter() {
            return new TestContextFormatter();
        }

        @Override
        public ResponseParse.ResponseParser responseParser() {
            return new TestResponseParser();
        }

        @Override
        public RequestExtend.RequestExtender requestExtender() {
            return new TestRequestExtender();
        }

        @Override
        public List<Pair<String, Object>> fixedContexts() {
            return newArrayList(Pair.of("C1", "V1"), Pair.of("C2", "V2"));
        }
    }

    public static class SampleMappingConfig implements FixedContextsConfigurer {

        @Override
        public List<Pair<String, Object>> fixedContexts() {
            return newArrayList(Pair.of("C2", null), Pair.of("C3", "V3"));
        }
    }

    public static class SampleMappingConfig2 extends SampleMappingConfig implements ResponseParseConfigurer, RequestExtendConfigurer {

        @Override
        public ResponseParse.ResponseParser responseParser() {
            return new TestResponseParser();
        }

        @Override
        public RequestExtend.RequestExtender requestExtender() {
            return new TestRequestExtender();
        }
    }

    public static class SampleMappingConfig3 implements ResponseParseDisabledConfigurer, RequestExtendDisabledConfigurer {
    }
}
