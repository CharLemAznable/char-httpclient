package com.github.charlemaznable.httpclient.common;

import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static com.github.charlemaznable.core.lang.Str.isNull;
import static com.github.charlemaznable.httpclient.common.CommonConstant.ACCEPT_CHARSET;
import static com.github.charlemaznable.httpclient.common.CommonConstant.CONTENT_TYPE;
import static com.github.charlemaznable.httpclient.common.Utils.dispatcher;
import static com.google.common.net.MediaType.FORM_DATA;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class CommonReqTest {

    protected MockWebServer mockWebServer;

    @SneakyThrows
    protected void startMockWebServer(int port) {
        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher(request -> {
            val requestUrl = requireNonNull(request.getRequestUrl());
            switch (requestUrl.encodedPath()) {
                case "/sample1" -> {
                    val acceptCharset = request.getHeader(ACCEPT_CHARSET);
                    assertEquals(ISO_8859_1.name(), acceptCharset);
                    val contentType = requireNonNull(request.getHeader(CONTENT_TYPE));
                    assertTrue(contentType.startsWith(FORM_DATA.toString()));
                    assertNull(request.getHeader("AAA"));
                    assertEquals("bbb", request.getHeader("BBB"));
                    assertEquals("ccc", requestUrl.queryParameter("CCC"));
                    assertEquals("GET", request.getMethod());
                    return new MockResponse().setBody("Sample1");
                }
                case "/sample2" -> {
                    assertEquals("BBB=bbb", request.getBody().readUtf8());
                    assertEquals("POST", request.getMethod());
                    return new MockResponse().setBody("Sample2");
                }
                case "/sample3" -> {
                    assertEquals("ddd", requestUrl.queryParameter("DDD"));
                    assertNull(requestUrl.queryParameter("AAA"));
                    assertEquals("bbb", requestUrl.queryParameter("BBB"));
                    assertNull(requestUrl.queryParameter("CCC"));
                    assertEquals("GET", request.getMethod());
                    return new MockResponse().setBody("Sample3");
                }
                case "/sample4" -> {
                    assertEquals("CCC=ccc", request.getBody().readUtf8());
                    assertEquals("POST", request.getMethod());
                    return new MockResponse().setBody("Sample4");
                }
                case "/sample5", "/sample6" -> {
                    if (isNull(requestUrl.queryParameter("AAA"))) {
                        return new MockResponse()
                                .setResponseCode(HttpStatus.NOT_FOUND.value())
                                .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    } else {
                        return new MockResponse()
                                .setResponseCode(HttpStatus.FORBIDDEN.value())
                                .setBody(HttpStatus.FORBIDDEN.getReasonPhrase());
                    }
                }
                case "/sample7" -> {
                    assertEquals("aaa", requestUrl.queryParameter("AAA"));
                    if ("GET".equals(request.getMethod())) {
                        assertEquals("bbb", requestUrl.queryParameter("BBB"));
                    } else if ("POST".equals(request.getMethod())) {
                        assertEquals("{\"BBB\":\"bbb\"}", request.getBody().readUtf8());
                    } else {
                        return new MockResponse()
                                .setResponseCode(HttpStatus.NOT_FOUND.value())
                                .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                    return new MockResponse().setBody("Sample7");
                }
                default -> {
                    return new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                }
            }
        }));
        mockWebServer.start(port);
    }

    @SneakyThrows
    protected void shutdownMockWebServer() {
        mockWebServer.shutdown();
    }
}
