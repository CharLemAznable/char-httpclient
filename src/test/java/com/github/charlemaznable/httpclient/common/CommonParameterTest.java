package com.github.charlemaznable.httpclient.common;

import com.github.charlemaznable.httpclient.annotation.ContentFormat;
import com.github.charlemaznable.httpclient.annotation.Parameter;
import com.github.charlemaznable.httpclient.configurer.ContentFormatConfigurer;
import com.github.charlemaznable.httpclient.configurer.FixedParametersConfigurer;
import com.google.common.base.Splitter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static com.github.charlemaznable.core.codec.Json.unJson;
import static com.github.charlemaznable.core.codec.Xml.unXml;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.httpclient.common.Utils.dispatcher;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class CommonParameterTest {

    protected MockWebServer getMockWebServer;
    protected MockWebServer postMockWebServer;

    @SneakyThrows
    protected void startGetMockWebServer() {
        getMockWebServer = new MockWebServer();
        getMockWebServer.setDispatcher(dispatcher(request -> {
            val requestUrl = requireNonNull(request.getRequestUrl());
            switch (requestUrl.encodedPath()) {
                case "/sampleDefault" -> {
                    assertNull(requestUrl.queryParameter("T0"));
                    assertEquals("V1", requestUrl.queryParameter("T1"));
                    assertEquals("V2", requestUrl.queryParameter("T2"));
                    assertNull(requestUrl.queryParameter("T3"));
                    assertNull(requestUrl.queryParameter("T4"));
                    return new MockResponse().setBody("OK");
                }
                case "/sampleMapping" -> {
                    assertEquals("V0", requestUrl.queryParameter("T0"));
                    assertEquals("V1", requestUrl.queryParameter("T1"));
                    assertNull(requestUrl.queryParameter("T2"));
                    assertEquals("V3", requestUrl.queryParameter("T3"));
                    assertNull(requestUrl.queryParameter("T4"));
                    return new MockResponse().setBody("OK");
                }
                case "/sampleParameters" -> {
                    assertEquals("V0", requestUrl.queryParameter("T0"));
                    assertEquals("V1", requestUrl.queryParameter("T1"));
                    assertNull(requestUrl.queryParameter("T2"));
                    assertNull(requestUrl.queryParameter("T3"));
                    assertEquals("V4", requestUrl.queryParameter("T4"));
                    return new MockResponse().setBody("OK");
                }
                case "/sampleBundle" -> {
                    assertEquals("V1", requestUrl.queryParameter("T1"));
                    assertNull(requestUrl.queryParameter("T2"));
                    assertNull(requestUrl.queryParameter("T3"));
                    assertEquals("V4", requestUrl.queryParameter("T4"));
                    assertEquals("V5", requestUrl.queryParameter("t5"));
                    assertEquals("V6", requestUrl.queryParameter("t6"));
                    return new MockResponse().setBody("OK");
                }
                case "/sampleBundle2" -> {
                    assertEquals("V1", requestUrl.queryParameter("T1"));
                    assertEquals("V2", requestUrl.queryParameter("T2"));
                    assertNull(requestUrl.queryParameter("T3"));
                    assertNull(requestUrl.queryParameter("T4"));
                    return new MockResponse().setBody("OK");
                }
                case "/sampleBundle3" -> {
                    assertEquals("V1", requestUrl.queryParameter("T1"));
                    assertEquals("V2", requestUrl.queryParameter("T2"));
                    assertNull(requestUrl.queryParameter("T3"));
                    assertEquals("V4", requestUrl.queryParameter("T4"));
                    assertEquals("V5", requestUrl.queryParameter("t5"));
                    return new MockResponse().setBody("OK");
                }
                default -> {
                    return new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                }
            }
        }));
        getMockWebServer.start(41160);
    }

    @SneakyThrows
    protected void shutdownGetMockWebServer() {
        getMockWebServer.shutdown();
    }

    @SneakyThrows
    protected void startPostMockWebServer() {
        postMockWebServer = new MockWebServer();
        postMockWebServer.setDispatcher(dispatcher(request -> {
            val body = request.getBody().readUtf8();
            switch (requireNonNull(request.getPath())) {
                case "/sampleDefault", "/sampleBundle2", "/sampleRawError" -> {
                    val defaultMap = Splitter.on("&")
                            .withKeyValueSeparator("=").split(body);
                    assertEquals("V1", defaultMap.get("T1"));
                    assertEquals("V2", defaultMap.get("T2"));
                    assertNull(defaultMap.get("T3"));
                    assertNull(defaultMap.get("T4"));
                    return new MockResponse().setBody("OK");
                }
                case "/sampleMapping" -> {
                    val mappingMap = unJson(body);
                    assertEquals("V1", mappingMap.get("T1"));
                    assertNull(mappingMap.get("T2"));
                    assertEquals("V3", mappingMap.get("T3"));
                    assertNull(mappingMap.get("T4"));
                    return new MockResponse().setBody("OK");
                }
                case "/sampleParameters" -> {
                    val paramMap = unXml(body);
                    assertEquals("V1", paramMap.get("T1"));
                    assertNull(paramMap.get("T2"));
                    assertNull(paramMap.get("T3"));
                    assertEquals("V4", paramMap.get("T4"));
                    return new MockResponse().setBody("OK");
                }
                case "/sampleBundle" -> {
                    val bundleMap = Splitter.on("&")
                            .withKeyValueSeparator("=").split(body);
                    assertEquals("V1", bundleMap.get("T1"));
                    assertNull(bundleMap.get("T2"));
                    assertNull(bundleMap.get("T3"));
                    assertEquals("V4", bundleMap.get("T4"));
                    assertEquals("V5", bundleMap.get("t5"));
                    assertEquals("V6", bundleMap.get("t6"));
                    return new MockResponse().setBody("OK");
                }
                case "/sampleBundle3" -> {
                    val bundleMap3 = Splitter.on("&")
                            .withKeyValueSeparator("=").split(body);
                    assertEquals("V1", bundleMap3.get("T1"));
                    assertEquals("V2", bundleMap3.get("T2"));
                    assertNull(bundleMap3.get("T3"));
                    assertEquals("V4", bundleMap3.get("T4"));
                    assertEquals("V5", bundleMap3.get("t5"));
                    return new MockResponse().setBody("OK");
                }
                case "/sampleRaw" -> {
                    val rawMap = Splitter.on("&")
                            .withKeyValueSeparator("=").split(body);
                    assertNull(rawMap.get("T1"));
                    assertNull(rawMap.get("T2"));
                    assertEquals("V3", rawMap.get("T3"));
                    assertEquals("V4", rawMap.get("T4"));
                    return new MockResponse().setBody("OK");
                }
                default -> {
                    return new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                }
            }
        }));
        postMockWebServer.start(41161);
    }

    @SneakyThrows
    protected void shutdownPostMockWebServer() {
        postMockWebServer.shutdown();
    }

    @Getter
    @Setter
    public static class BaseBundle {

        private String t6 = "V6";
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class TestBundle extends BaseBundle {

        @Parameter("T2")
        private String t2;
        @Parameter("T3")
        private String t3;
        @Parameter("T4")
        private String t4;
        private String t5;
    }

    public static class ParameterHttpClientConfig implements ContentFormatConfigurer, FixedParametersConfigurer {

        @Override
        public ContentFormat.ContentFormatter contentFormatter() {
            return new ContentFormat.FormContentFormatter();
        }

        @Override
        public List<Pair<String, Object>> fixedParameters() {
            return newArrayList(Pair.of("T1", "V1"), Pair.of("T2", "V2"));
        }
    }

    public static class SampleMappingConfig implements ContentFormatConfigurer, FixedParametersConfigurer {

        @Override
        public ContentFormat.ContentFormatter contentFormatter() {
            return new ContentFormat.JsonContentFormatter();
        }

        @Override
        public List<Pair<String, Object>> fixedParameters() {
            return newArrayList(Pair.of("T2", null), Pair.of("T3", "V3"));
        }
    }

    public static class SampleParametersConfig implements ContentFormatConfigurer, FixedParametersConfigurer {

        @Override
        public ContentFormat.ContentFormatter contentFormatter() {
            return new ContentFormat.TextXmlContentFormatter();
        }

        @Override
        public List<Pair<String, Object>> fixedParameters() {
            return newArrayList(Pair.of("T2", null), Pair.of("T3", "V3"));
        }
    }
}
