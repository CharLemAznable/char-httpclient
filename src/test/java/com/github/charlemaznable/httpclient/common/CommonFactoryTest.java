package com.github.charlemaznable.httpclient.common;

import com.github.charlemaznable.httpclient.annotation.ContentFormat;
import com.github.charlemaznable.httpclient.configurer.AcceptCharsetConfigurer;
import com.github.charlemaznable.httpclient.configurer.ContentFormatConfigurer;
import com.github.charlemaznable.httpclient.configurer.RequestMethodConfigurer;
import com.google.common.net.MediaType;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import java.nio.charset.Charset;

import static com.github.charlemaznable.httpclient.common.CommonConstant.ACCEPT_CHARSET;
import static com.github.charlemaznable.httpclient.common.CommonConstant.CONTENT_TYPE;
import static com.github.charlemaznable.httpclient.common.Utils.dispatcher;
import static com.google.common.net.MediaType.APPLICATION_XML_UTF_8;
import static com.google.common.net.MediaType.FORM_DATA;
import static com.google.common.net.MediaType.JSON_UTF_8;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class CommonFactoryTest {

    private static final String SAMPLE = "/sample";
    private static final String SAMPLE2 = "/sample2";
    private static final String SAMPLE3 = "/sample3";
    private static final String COVER = "/cover";

    protected MockWebServer mockWebServer1;
    protected MockWebServer mockWebServer2;
    protected MockWebServer mockWebServer3;
    protected MockWebServer mockWebServer4;

    @SneakyThrows
    protected void startMockWebServer1() {
        mockWebServer1 = new MockWebServer();
        mockWebServer1.setDispatcher(dispatcher(request -> {
            val acceptCharset = requireNonNull(request.getHeader(ACCEPT_CHARSET));
            switch (requireNonNull(request.getPath())) {
                case SAMPLE -> {
                    assertEquals(ISO_8859_1.name(), acceptCharset);
                    return new MockResponse().setBody(acceptCharset);
                }
                case SAMPLE2, COVER -> {
                    assertEquals(UTF_8.name(), acceptCharset);
                    return new MockResponse().setBody(acceptCharset);
                }
                default -> {
                    return new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                }
            }
        }));
        mockWebServer1.start(41130);
    }

    @SneakyThrows
    protected void shutdownMockWebServer1() {
        mockWebServer1.shutdown();
    }

    @SneakyThrows
    protected void startMockWebServer2() {
        mockWebServer2 = new MockWebServer();
        mockWebServer2.setDispatcher(dispatcher(request -> {
            val contentType = MediaType.parse(requireNonNull(request.getHeader(CONTENT_TYPE)));
            val bodyString = request.getBody().readUtf8();
            switch (requireNonNull(request.getPath())) {
                case SAMPLE -> {
                    assertEquals(contentType.type(), FORM_DATA.type());
                    assertEquals(contentType.subtype(), FORM_DATA.subtype());
                    return new MockResponse().setBody(bodyString);
                }
                case SAMPLE2 -> {
                    assertEquals(contentType.type(), JSON_UTF_8.type());
                    assertEquals(contentType.subtype(), JSON_UTF_8.subtype());
                    assertEquals(contentType.charset(), JSON_UTF_8.charset());
                    return new MockResponse().setBody(bodyString);
                }
                case SAMPLE3, COVER -> {
                    assertEquals(contentType.type(), APPLICATION_XML_UTF_8.type());
                    assertEquals(contentType.subtype(), APPLICATION_XML_UTF_8.subtype());
                    assertEquals(contentType.charset(), APPLICATION_XML_UTF_8.charset());
                    return new MockResponse().setBody(bodyString);
                }
                default -> {
                    return new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                }
            }
        }));
        mockWebServer2.start(41131);
    }

    @SneakyThrows
    protected void shutdownMockWebServer2() {
        mockWebServer2.shutdown();
    }

    @SneakyThrows
    protected void startMockWebServer3() {
        mockWebServer3 = new MockWebServer();
        mockWebServer3.setDispatcher(dispatcher(request -> {
            val method = requireNonNull(request.getMethod());
            switch (requireNonNull(request.getPath())) {
                case SAMPLE -> {
                    assertEquals("POST", method);
                    return new MockResponse().setBody(method);
                }
                case SAMPLE2, COVER -> {
                    assertEquals("GET", method);
                    return new MockResponse().setBody(method);
                }
                default -> {
                    return new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                }
            }
        }));
        mockWebServer3.start(41132);
    }

    @SneakyThrows
    protected void shutdownMockWebServer3() {
        mockWebServer3.shutdown();
    }

    @SneakyThrows
    protected void startMockWebServer4() {
        mockWebServer4 = new MockWebServer();
        mockWebServer4.setDispatcher(dispatcher(request -> {
            if (SAMPLE.equals(request.getPath())) {
                return new MockResponse().setBody("OK");
            } else {
                return new MockResponse()
                        .setResponseCode(HttpStatus.NOT_FOUND.value())
                        .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
            }
        }));
        mockWebServer4.start(41133);
    }

    @SneakyThrows
    protected void shutdownMockWebServer4() {
        mockWebServer4.shutdown();
    }

    public static class TestNotInterface {

        public void test() {
            // empty
        }
    }

    public static class AcceptCharsetHttpClientConfig implements AcceptCharsetConfigurer {

        @Override
        public Charset acceptCharset() {
            return ISO_8859_1;
        }
    }

    public static class AcceptCharsetHttpClientSample2Config implements AcceptCharsetConfigurer {

        @Override
        public Charset acceptCharset() {
            return UTF_8;
        }
    }

    public static class ContentFormatHttpClientConfig implements ContentFormatConfigurer {

        @Override
        public ContentFormat.ContentFormatter contentFormatter() {
            return new ContentFormat.FormContentFormatter();
        }
    }

    public static class ContentFormatHttpClientSample2Config implements ContentFormatConfigurer {

        @Override
        public ContentFormat.ContentFormatter contentFormatter() {
            return new ContentFormat.JsonContentFormatter();
        }
    }

    public static class ContentFormatHttpClientSample3Config implements ContentFormatConfigurer {

        @Override
        public ContentFormat.ContentFormatter contentFormatter() {
            return new ContentFormat.ApplicationXmlContentFormatter();
        }
    }

    public static class RequestMethodHttpClientConfig implements RequestMethodConfigurer {

        @Override
        public HttpMethod requestMethod() {
            return HttpMethod.POST;
        }
    }

    public static class RequestMethodHttpClientSample2Config implements RequestMethodConfigurer {

        @Override
        public HttpMethod requestMethod() {
            return HttpMethod.GET;
        }
    }
}
