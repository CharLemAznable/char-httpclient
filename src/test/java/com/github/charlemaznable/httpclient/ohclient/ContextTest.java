package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.core.lang.Mapp;
import com.github.charlemaznable.httpclient.common.ConfigureWith;
import com.github.charlemaznable.httpclient.common.ContentFormat;
import com.github.charlemaznable.httpclient.common.ContentFormat.ContentFormatter;
import com.github.charlemaznable.httpclient.common.Context;
import com.github.charlemaznable.httpclient.common.FixedContext;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.common.RequestExtend;
import com.github.charlemaznable.httpclient.common.RequestExtend.RequestExtender;
import com.github.charlemaznable.httpclient.common.RequestMethod;
import com.github.charlemaznable.httpclient.common.ResponseParse;
import com.github.charlemaznable.httpclient.common.ResponseParse.ResponseParser;
import com.github.charlemaznable.httpclient.configurer.ContentFormatConfigurer;
import com.github.charlemaznable.httpclient.configurer.FixedContextsConfigurer;
import com.github.charlemaznable.httpclient.configurer.MappingConfigurer;
import com.github.charlemaznable.httpclient.configurer.RequestExtendConfigurer;
import com.github.charlemaznable.httpclient.configurer.RequestExtendDisabledConfigurer;
import com.github.charlemaznable.httpclient.configurer.RequestMethodConfigurer;
import com.github.charlemaznable.httpclient.configurer.ResponseParseConfigurer;
import com.github.charlemaznable.httpclient.configurer.ResponseParseDisabledConfigurer;
import com.github.charlemaznable.httpclient.ohclient.OhFactory.OhLoader;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.codec.Json.unJson;
import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static com.google.common.net.MediaType.JSON_UTF_8;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ContextTest {

    private static final OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @SneakyThrows
    @Test
    public void testOhContext() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
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
                }
            });
            mockWebServer.start(41170);

            val httpClient = ohLoader.getClient(ContextHttpClient.class);
            assertEquals("OK", httpClient.sampleDefault());
            assertEquals("OK", httpClient.sampleMapping());
            assertEquals("OK", httpClient.sampleContexts(null, "V4"));
            assertEquals("OK", httpClient.sampleDefaultResponse().getResponse());
            assertEquals("OK", httpClient.sampleMappingResponse().getResponse());
            assertEquals("OK", httpClient.sampleContextsResponse(null, "V4").getResponse());
            assertEquals("OK", httpClient.sampleNone());

            val httpClientNeo = ohLoader.getClient(ContextHttpClientNeo.class);
            assertEquals("OK", httpClientNeo.sampleDefault());
            assertEquals("OK", httpClientNeo.sampleMapping());
            assertEquals("OK", httpClientNeo.sampleContexts(null, "V4"));
            assertEquals("OK", httpClientNeo.sampleDefaultResponse().getResponse());
            assertEquals("OK", httpClientNeo.sampleMappingResponse().getResponse());
            assertEquals("OK", httpClientNeo.sampleContextsResponse(null, "V4").getResponse());
            assertEquals("OK", httpClientNeo.sampleNone());
        }
    }

    @RequestMethod(HttpMethod.POST)
    @ContentFormat(TestContextFormatter.class)
    @ResponseParse(TestResponseParser.class)
    @RequestExtend(TestRequestExtender.class)
    @FixedContext(name = "C1", value = "V1")
    @FixedContext(name = "C2", value = "V2")
    @Mapping("${root}:41170")
    @OhClient
    public interface ContextHttpClient {

        String sampleDefault();

        @FixedContext(name = "C2")
        @FixedContext(name = "C3", value = "V3")
        String sampleMapping();

        @FixedContext(name = "C2")
        @FixedContext(name = "C3", value = "V3")
        String sampleContexts(@Context("C3") String v3,
                              @Context("C4") String v4);

        @ResponseParse(TestResponseParser.class)
        @RequestExtend(TestRequestExtender.class)
        @Mapping("/sampleDefault")
        TestResponse sampleDefaultResponse();

        @ResponseParse(TestResponseParser.class)
        @RequestExtend(TestRequestExtender.class)
        @Mapping("/sampleMapping")
        @FixedContext(name = "C2")
        @FixedContext(name = "C3", value = "V3")
        TestResponse sampleMappingResponse();

        @ResponseParse(TestResponseParser.class)
        @RequestExtend(TestRequestExtender.class)
        @Mapping("/sampleContexts")
        @FixedContext(name = "C2")
        @FixedContext(name = "C3", value = "V3")
        TestResponse sampleContextsResponse(@Context("C3") String v3,
                                            @Context("C4") String v4);

        @ResponseParse.Disabled
        @RequestExtend.Disabled
        String sampleNone();
    }

    public static class TestContextFormatter implements ContentFormatter {

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

    public static class TestResponseParser implements ResponseParser {

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

    public static class TestRequestExtender implements RequestExtender {

        @Override
        public void extend(List<Pair<String, String>> headers,
                           List<Pair<String, String>> pathVars,
                           List<Pair<String, Object>> parameters,
                           List<Pair<String, Object>> contexts) {
            headers.add(Pair.of("H1", "EH1"));
        }
    }

    @OhClient
    @ConfigureWith(ContextHttpClientConfig.class)
    public interface ContextHttpClientNeo {

        String sampleDefault();

        @ConfigureWith(SampleMappingConfig.class)
        String sampleMapping();

        @ConfigureWith(SampleMappingConfig.class)
        String sampleContexts(@Context("C3") String v3,
                              @Context("C4") String v4);

        @ResponseParse(TestResponseParser.class)
        @RequestExtend(TestRequestExtender.class)
        @Mapping("/sampleDefault")
        TestResponse sampleDefaultResponse();

        @Mapping("/sampleMapping")
        @ConfigureWith(SampleMappingConfig2.class)
        TestResponse sampleMappingResponse();

        @Mapping("/sampleContexts")
        @ConfigureWith(SampleMappingConfig2.class)
        TestResponse sampleContextsResponse(@Context("C3") String v3,
                                            @Context("C4") String v4);

        @ConfigureWith(SampleMappingConfig3.class)
        String sampleNone();
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
        public ContentFormatter contentFormatter() {
            return new TestContextFormatter();
        }

        @Override
        public ResponseParser responseParser() {
            return new TestResponseParser();
        }

        @Override
        public RequestExtender requestExtender() {
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
        public ResponseParser responseParser() {
            return new TestResponseParser();
        }

        @Override
        public RequestExtender requestExtender() {
            return new TestRequestExtender();
        }
    }

    public static class SampleMappingConfig3 implements ResponseParseDisabledConfigurer, RequestExtendDisabledConfigurer {}
}
