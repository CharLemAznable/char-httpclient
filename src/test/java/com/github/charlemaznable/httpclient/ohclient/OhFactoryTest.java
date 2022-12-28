package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.common.AcceptCharset;
import com.github.charlemaznable.httpclient.common.ContentFormat;
import com.github.charlemaznable.httpclient.common.ContentFormat.ApplicationXmlContentFormatter;
import com.github.charlemaznable.httpclient.common.ContentFormat.FormContentFormatter;
import com.github.charlemaznable.httpclient.common.ContentFormat.JsonContentFormatter;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.common.RequestMethod;
import com.github.charlemaznable.httpclient.ohclient.OhFactory.OhLoader;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.ACCEPT_CHARSET;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.CONTENT_TYPE;
import static com.google.common.net.MediaType.APPLICATION_XML_UTF_8;
import static com.google.common.net.MediaType.FORM_DATA;
import static com.google.common.net.MediaType.JSON_UTF_8;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OhFactoryTest {

    private static final String SAMPLE = "/sample";
    private static final String SAMPLE2 = "/sample2";
    private static final String STRING_PREFIX = "OhClient:";
    private static final OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @Test
    public void testOhFactory() {
        assertThrows(OhException.class, () -> ohLoader.getClient(TestNotInterface.class));
    }

    @SneakyThrows
    @Test
    public void testAcceptCharset() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    val acceptCharset = requireNonNull(request.getHeader(ACCEPT_CHARSET));
                    switch (requireNonNull(request.getPath())) {
                        case SAMPLE:
                            assertEquals(ISO_8859_1.name(), acceptCharset);
                            return new MockResponse().setBody(acceptCharset);
                        case SAMPLE2:
                            assertEquals(UTF_8.name(), acceptCharset);
                            return new MockResponse().setBody(acceptCharset);
                        default:
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            });
            mockWebServer.start(41130);

            val httpClient = ohLoader.getClient(AcceptCharsetHttpClient.class);
            assertEquals(ISO_8859_1.name(), httpClient.sample());
            assertEquals(UTF_8.name(), httpClient.sample2());

            assertEquals(STRING_PREFIX + AcceptCharsetHttpClient.class.getSimpleName() + "@"
                    + Integer.toHexString(httpClient.hashCode()), httpClient.toString());
            assertEquals(httpClient, ohLoader.getClient(AcceptCharsetHttpClient.class));
            assertEquals(httpClient.hashCode(), ohLoader.getClient(AcceptCharsetHttpClient.class).hashCode());
        }
    }

    @SneakyThrows
    @Test
    public void testContentFormat() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    val contentType = requireNonNull(request.getHeader(CONTENT_TYPE));
                    val bodyString = request.getBody().readUtf8();
                    switch (requireNonNull(request.getPath())) {
                        case SAMPLE:
                            assertTrue(contentType.startsWith(FORM_DATA.toString()));
                            return new MockResponse().setBody(bodyString);
                        case SAMPLE2:
                            assertTrue(contentType.startsWith(JSON_UTF_8.toString()));
                            return new MockResponse().setBody(bodyString);
                        case "/sample3":
                            assertTrue(contentType.startsWith(APPLICATION_XML_UTF_8.toString()));
                            return new MockResponse().setBody(bodyString);
                        default:
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            });
            mockWebServer.start(41131);

            val httpClient = ohLoader.getClient(ContentFormatHttpClient.class);
            assertEquals("", httpClient.sample());
            assertEquals("{}", httpClient.sample2());
            assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xml/>", httpClient.sample3());

            assertEquals(STRING_PREFIX + ContentFormatHttpClient.class.getSimpleName() + "@"
                    + Integer.toHexString(httpClient.hashCode()), httpClient.toString());
            assertEquals(httpClient, ohLoader.getClient(ContentFormatHttpClient.class));
            assertEquals(httpClient.hashCode(), ohLoader.getClient(ContentFormatHttpClient.class).hashCode());
        }
    }

    @SneakyThrows
    @Test
    public void testRequestMethod() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    val method = requireNonNull(request.getMethod());
                    switch (requireNonNull(request.getPath())) {
                        case SAMPLE:
                            assertEquals("POST", method);
                            return new MockResponse().setBody(method);
                        case SAMPLE2:
                            assertEquals("GET", method);
                            return new MockResponse().setBody(method);
                        default:
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            });
            mockWebServer.start(41132);

            val httpClient = ohLoader.getClient(RequestMethodHttpClient.class);
            assertEquals("POST", httpClient.sample());
            assertEquals("GET", httpClient.sample2());

            assertEquals(STRING_PREFIX + RequestMethodHttpClient.class.getSimpleName() + "@"
                    + Integer.toHexString(httpClient.hashCode()), httpClient.toString());
            assertEquals(httpClient, ohLoader.getClient(RequestMethodHttpClient.class));
            assertEquals(httpClient.hashCode(), ohLoader.getClient(RequestMethodHttpClient.class).hashCode());
        }
    }

    @SneakyThrows
    @Test
    public void testExtendInterface() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    if (SAMPLE.equals(request.getPath())) {
                        return new MockResponse().setBody("OK");
                    } else {
                        return new MockResponse()
                                .setResponseCode(HttpStatus.NOT_FOUND.value())
                                .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            });
            mockWebServer.start(41133);

            val baseHttpClient = ohLoader.getClient(BaseHttpClient.class);
            assertNotNull(baseHttpClient);

            assertThrows(OhException.class, () -> ohLoader.getClient(SubHttpClient.class));
        }
    }

    @AcceptCharset("ISO-8859-1")
    @Mapping("${root}:41130")
    @OhClient
    public interface AcceptCharsetHttpClient {

        String sample();

        @AcceptCharset("UTF-8")
        String sample2();
    }

    @RequestMethod(HttpMethod.POST)
    @ContentFormat(FormContentFormatter.class)
    @Mapping("${root}:41131")
    @OhClient
    public interface ContentFormatHttpClient {

        String sample();

        @ContentFormat(JsonContentFormatter.class)
        String sample2();

        @ContentFormat(ApplicationXmlContentFormatter.class)
        String sample3();
    }

    @RequestMethod(HttpMethod.POST)
    @Mapping("${root}:41132")
    @OhClient
    public interface RequestMethodHttpClient {

        String sample();

        @RequestMethod(HttpMethod.GET)
        String sample2();
    }

    @Mapping("${root}:41133")
    @OhClient
    public interface BaseHttpClient {
    }

    public interface SubHttpClient extends BaseHttpClient {
    }

    public static class TestNotInterface {

        public void test() {
            // empty
        }
    }
}
