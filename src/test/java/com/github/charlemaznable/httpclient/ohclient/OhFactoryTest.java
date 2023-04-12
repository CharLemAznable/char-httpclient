package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.annotation.AcceptCharset;
import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.ContentFormat;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.RequestMethod;
import com.github.charlemaznable.httpclient.common.CommonFactoryTest;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import com.github.charlemaznable.httpclient.ohclient.OhFactory.OhLoader;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OhFactoryTest extends CommonFactoryTest {

    private static final String STRING_PREFIX = "OhClient:";
    private static final OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @Test
    public void testOhFactory() {
        assertThrows(OhException.class, () -> ohLoader.getClient(TestNotInterface.class));
    }

    @Test
    public void testAcceptCharset() {
        startMockWebServer1();

        val httpClient = ohLoader.getClient(AcceptCharsetHttpClient.class);
        assertEquals(ISO_8859_1.name(), httpClient.sample());
        assertEquals(UTF_8.name(), httpClient.sample2());
        assertEquals(UTF_8.name(), httpClient.cover(UTF_8));

        assertEquals(STRING_PREFIX + AcceptCharsetHttpClient.class.getSimpleName() + "@"
                + Integer.toHexString(httpClient.hashCode()), httpClient.toString());
        assertEquals(httpClient, ohLoader.getClient(AcceptCharsetHttpClient.class));
        assertEquals(httpClient.hashCode(), ohLoader.getClient(AcceptCharsetHttpClient.class).hashCode());

        val httpClientNeo = ohLoader.getClient(AcceptCharsetHttpClientNeo.class);
        assertEquals(ISO_8859_1.name(), httpClientNeo.sample());
        assertEquals(UTF_8.name(), httpClientNeo.sample2());

        assertEquals(STRING_PREFIX + AcceptCharsetHttpClientNeo.class.getSimpleName() + "@"
                + Integer.toHexString(httpClientNeo.hashCode()), httpClientNeo.toString());
        assertEquals(httpClientNeo, ohLoader.getClient(AcceptCharsetHttpClientNeo.class));
        assertEquals(httpClientNeo.hashCode(), ohLoader.getClient(AcceptCharsetHttpClientNeo.class).hashCode());

        shutdownMockWebServer1();
    }

    @Test
    public void testContentFormat() {
        startMockWebServer2();

        val httpClient = ohLoader.getClient(ContentFormatHttpClient.class);
        assertEquals("", httpClient.sample());
        assertEquals("{}", httpClient.sample2());
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xml/>", httpClient.sample3());
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xml/>", httpClient.cover(new ContentFormat.ApplicationXmlContentFormatter()));

        assertEquals(STRING_PREFIX + ContentFormatHttpClient.class.getSimpleName() + "@"
                + Integer.toHexString(httpClient.hashCode()), httpClient.toString());
        assertEquals(httpClient, ohLoader.getClient(ContentFormatHttpClient.class));
        assertEquals(httpClient.hashCode(), ohLoader.getClient(ContentFormatHttpClient.class).hashCode());

        val httpClientNeo = ohLoader.getClient(ContentFormatHttpClientNeo.class);
        assertEquals("", httpClientNeo.sample());
        assertEquals("{}", httpClientNeo.sample2());
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xml/>", httpClientNeo.sample3());

        assertEquals(STRING_PREFIX + ContentFormatHttpClientNeo.class.getSimpleName() + "@"
                + Integer.toHexString(httpClientNeo.hashCode()), httpClientNeo.toString());
        assertEquals(httpClientNeo, ohLoader.getClient(ContentFormatHttpClientNeo.class));
        assertEquals(httpClientNeo.hashCode(), ohLoader.getClient(ContentFormatHttpClientNeo.class).hashCode());

        shutdownMockWebServer2();
    }

    @Test
    public void testRequestMethod() {
        startMockWebServer3();

        val httpClient = ohLoader.getClient(RequestMethodHttpClient.class);
        assertEquals("POST", httpClient.sample());
        assertEquals("GET", httpClient.sample2());
        assertEquals("GET", httpClient.cover(HttpMethod.GET));

        assertEquals(STRING_PREFIX + RequestMethodHttpClient.class.getSimpleName() + "@"
                + Integer.toHexString(httpClient.hashCode()), httpClient.toString());
        assertEquals(httpClient, ohLoader.getClient(RequestMethodHttpClient.class));
        assertEquals(httpClient.hashCode(), ohLoader.getClient(RequestMethodHttpClient.class).hashCode());

        val httpClientNeo = ohLoader.getClient(RequestMethodHttpClientNeo.class);
        assertEquals("POST", httpClientNeo.sample());
        assertEquals("GET", httpClientNeo.sample2());

        assertEquals(STRING_PREFIX + RequestMethodHttpClientNeo.class.getSimpleName() + "@"
                + Integer.toHexString(httpClientNeo.hashCode()), httpClientNeo.toString());
        assertEquals(httpClientNeo, ohLoader.getClient(RequestMethodHttpClientNeo.class));
        assertEquals(httpClientNeo.hashCode(), ohLoader.getClient(RequestMethodHttpClientNeo.class).hashCode());

        shutdownMockWebServer3();
    }

    @Test
    public void testExtendInterface() {
        startMockWebServer4();

        val baseHttpClient = ohLoader.getClient(BaseHttpClient.class);
        assertNotNull(baseHttpClient);

        assertThrows(OhException.class, () -> ohLoader.getClient(SubHttpClient.class));

        shutdownMockWebServer4();
    }

    @AcceptCharset("ISO-8859-1")
    @Mapping("${root}:41130")
    @OhClient
    public interface AcceptCharsetHttpClient {

        String sample();

        @AcceptCharset("UTF-8")
        String sample2();

        String cover(Charset acceptCharset);
    }

    @RequestMethod(HttpMethod.POST)
    @ContentFormat(ContentFormat.FormContentFormatter.class)
    @Mapping("${root}:41131")
    @OhClient
    public interface ContentFormatHttpClient {

        String sample();

        @ContentFormat(ContentFormat.JsonContentFormatter.class)
        String sample2();

        @ContentFormat(ContentFormat.ApplicationXmlContentFormatter.class)
        String sample3();

        String cover(ContentFormat.ContentFormatter contentFormatter);
    }

    @RequestMethod(HttpMethod.POST)
    @Mapping("${root}:41132")
    @OhClient
    public interface RequestMethodHttpClient {

        String sample();

        @RequestMethod(HttpMethod.GET)
        String sample2();

        String cover(HttpMethod httpMethod);
    }

    @Mapping("${root}:41133")
    @OhClient
    public interface BaseHttpClient {
    }

    public interface SubHttpClient extends BaseHttpClient {
    }

    @Mapping("${root}:41130")
    @OhClient
    @ConfigureWith(AcceptCharsetHttpClientConfig.class)
    public interface AcceptCharsetHttpClientNeo {

        String sample();

        @ConfigureWith(AcceptCharsetHttpClientSample2Config.class)
        String sample2();
    }

    @RequestMethod(HttpMethod.POST)
    @Mapping("${root}:41131")
    @OhClient
    @ConfigureWith(ContentFormatHttpClientConfig.class)
    public interface ContentFormatHttpClientNeo {

        String sample();

        @ConfigureWith(ContentFormatHttpClientSample2Config.class)
        String sample2();

        @ConfigureWith(ContentFormatHttpClientSample3Config.class)
        String sample3();
    }

    @Mapping("${root}:41132")
    @OhClient
    @ConfigureWith(RequestMethodHttpClientConfig.class)
    public interface RequestMethodHttpClientNeo {

        String sample();

        @ConfigureWith(RequestMethodHttpClientSample2Config.class)
        String sample2();
    }
}
