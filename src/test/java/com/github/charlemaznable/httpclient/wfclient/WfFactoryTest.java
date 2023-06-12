package com.github.charlemaznable.httpclient.wfclient;

import com.github.charlemaznable.httpclient.annotation.AcceptCharset;
import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.ContentFormat;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.RequestMethod;
import com.github.charlemaznable.httpclient.common.CommonFactoryTest;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import lombok.val;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.Optional;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WfFactoryTest extends CommonFactoryTest {

    private static final String STRING_PREFIX = "WfClient:";
    private static final WfFactory.WfLoader wfLoader = WfFactory.wfLoader(reflectFactory());

    @Test
    public void testWfFactory() {
        assertThrows(WfException.class, () -> wfLoader.getClient(TestNotInterface.class));
    }

    @Test
    public void testAcceptCharset() {
        startMockWebServer1();

        val httpClient = wfLoader.getClient(AcceptCharsetHttpClient.class);
        assertEquals(ISO_8859_1.name(), httpClient.sample().block());
        assertEquals(UTF_8.name(), httpClient.sample2().block());
        assertEquals(UTF_8.name(), httpClient.cover(UTF_8).block());

        assertEquals(STRING_PREFIX + AcceptCharsetHttpClient.class.getSimpleName() + "@"
                + Integer.toHexString(httpClient.hashCode()), httpClient.toString());
        assertEquals(httpClient, wfLoader.getClient(AcceptCharsetHttpClient.class));
        assertEquals(httpClient.hashCode(), wfLoader.getClient(AcceptCharsetHttpClient.class).hashCode());

        val httpClientNeo = wfLoader.getClient(AcceptCharsetHttpClientNeo.class);
        assertEquals(ISO_8859_1.name(), httpClientNeo.sample().block());
        assertEquals(UTF_8.name(), httpClientNeo.sample2().block());

        assertEquals(STRING_PREFIX + AcceptCharsetHttpClientNeo.class.getSimpleName() + "@"
                + Integer.toHexString(httpClientNeo.hashCode()), httpClientNeo.toString());
        assertEquals(httpClientNeo, wfLoader.getClient(AcceptCharsetHttpClientNeo.class));
        assertEquals(httpClientNeo.hashCode(), wfLoader.getClient(AcceptCharsetHttpClientNeo.class).hashCode());

        shutdownMockWebServer1();
    }

    @Test
    public void testContentFormat() {
        startMockWebServer2();

        val httpClient = wfLoader.getClient(ContentFormatHttpClient.class);
        assertEquals("", httpClient.sample().block());
        assertEquals("{}", httpClient.sample2().block());
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xml/>", httpClient.sample3().block());
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xml/>", httpClient.cover(new ContentFormat.ApplicationXmlContentFormatter()).block());

        assertEquals(STRING_PREFIX + ContentFormatHttpClient.class.getSimpleName() + "@"
                + Integer.toHexString(httpClient.hashCode()), httpClient.toString());
        assertEquals(httpClient, wfLoader.getClient(ContentFormatHttpClient.class));
        assertEquals(httpClient.hashCode(), wfLoader.getClient(ContentFormatHttpClient.class).hashCode());

        val httpClientNeo = wfLoader.getClient(ContentFormatHttpClientNeo.class);
        assertEquals("", httpClientNeo.sample().block());
        assertEquals("{}", httpClientNeo.sample2().block());
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xml/>", httpClientNeo.sample3().block());

        assertEquals(STRING_PREFIX + ContentFormatHttpClientNeo.class.getSimpleName() + "@"
                + Integer.toHexString(httpClientNeo.hashCode()), httpClientNeo.toString());
        assertEquals(httpClientNeo, wfLoader.getClient(ContentFormatHttpClientNeo.class));
        assertEquals(httpClientNeo.hashCode(), wfLoader.getClient(ContentFormatHttpClientNeo.class).hashCode());

        shutdownMockWebServer2();
    }

    @Test
    public void testRequestMethod() {
        startMockWebServer3();

        val httpClient = wfLoader.getClient(RequestMethodHttpClient.class);
        assertEquals("POST", httpClient.sample().block());
        assertEquals("GET", httpClient.sample2().block());
        assertEquals("GET", httpClient.cover(HttpMethod.GET).block());

        assertEquals(STRING_PREFIX + RequestMethodHttpClient.class.getSimpleName() + "@"
                + Integer.toHexString(httpClient.hashCode()), httpClient.toString());
        assertEquals(httpClient, wfLoader.getClient(RequestMethodHttpClient.class));
        assertEquals(httpClient.hashCode(), wfLoader.getClient(RequestMethodHttpClient.class).hashCode());

        val httpClientNeo = wfLoader.getClient(RequestMethodHttpClientNeo.class);
        assertEquals("POST", httpClientNeo.sample().block());
        assertEquals("GET", httpClientNeo.sample2().block());

        assertEquals(STRING_PREFIX + RequestMethodHttpClientNeo.class.getSimpleName() + "@"
                + Integer.toHexString(httpClientNeo.hashCode()), httpClientNeo.toString());
        assertEquals(httpClientNeo, wfLoader.getClient(RequestMethodHttpClientNeo.class));
        assertEquals(httpClientNeo.hashCode(), wfLoader.getClient(RequestMethodHttpClientNeo.class).hashCode());

        shutdownMockWebServer3();
    }

    @Test
    public void testExtendInterface() {
        startMockWebServer4();

        val baseHttpClient = wfLoader.getClient(BaseHttpClient.class);
        assertNotNull(baseHttpClient);

        assertThrows(WfException.class, () -> wfLoader.getClient(SubHttpClient.class));

        shutdownMockWebServer4();
    }

    @AcceptCharset("ISO-8859-1")
    @Mapping("${root}:41130")
    @WfClient
    public interface AcceptCharsetHttpClient {

        Mono<String> sample();

        @AcceptCharset("UTF-8")
        Mono<String> sample2();

        Mono<String> cover(Charset acceptCharset);
    }

    @RequestMethod(HttpMethod.POST)
    @ContentFormat(ContentFormat.FormContentFormatter.class)
    @Mapping("${root}:41131")
    @WfClient
    public interface ContentFormatHttpClient {

        Mono<String> sample();

        @ContentFormat(ContentFormat.JsonContentFormatter.class)
        Mono<String> sample2();

        @ContentFormat(ContentFormat.ApplicationXmlContentFormatter.class)
        Mono<String> sample3();

        Mono<String> cover(ContentFormat.ContentFormatter contentFormatter);
    }

    @RequestMethod(HttpMethod.POST)
    @Mapping("${root}:41132")
    @WfClient
    public interface RequestMethodHttpClient {

        Mono<String> sample();

        @RequestMethod(HttpMethod.GET)
        Mono<String> sample2();

        Mono<String> cover(HttpMethod httpMethod);
    }

    @Mapping("${root}:41133")
    @WfClient
    public interface BaseHttpClient {
    }

    public interface SubHttpClient extends BaseHttpClient {
    }

    @Mapping("${root}:41130")
    @WfClient
    @ConfigureWith(AcceptCharsetHttpClientConfig.class)
    public interface AcceptCharsetHttpClientNeo {

        Mono<String> sample();

        @ConfigureWith(AcceptCharsetHttpClientSample2Config.class)
        Mono<String> sample2();
    }

    @RequestMethod(HttpMethod.POST)
    @Mapping("${root}:41131")
    @WfClient
    @ConfigureWith(ContentFormatHttpClientConfig.class)
    public interface ContentFormatHttpClientNeo {

        Mono<String> sample();

        @ConfigureWith(ContentFormatHttpClientSample2Config.class)
        Mono<String> sample2();

        @ConfigureWith(ContentFormatHttpClientSample3Config.class)
        Mono<String> sample3();
    }

    @Mapping("${root}:41132")
    @WfClient
    @ConfigureWith(RequestMethodHttpClientConfig.class)
    public interface RequestMethodHttpClientNeo {

        Mono<String> sample();

        @ConfigureWith(RequestMethodHttpClientSample2Config.class)
        Mono<String> sample2();
    }
}
