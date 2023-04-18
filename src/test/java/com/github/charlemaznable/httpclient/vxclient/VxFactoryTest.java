package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.httpclient.annotation.AcceptCharset;
import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.ContentFormat;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.RequestMethod;
import com.github.charlemaznable.httpclient.common.CommonFactoryTest;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.Charset;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(VertxExtension.class)
public class VxFactoryTest extends CommonFactoryTest {

    private static final String STRING_PREFIX = "VxClient:";

    @Test
    public void testVxFactory(Vertx vertx, VertxTestContext test) {
        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));
        assertThrows(VxException.class, () -> vxLoader.getClient(TestNotInterface.class));
        val vxLoader2 = VxFactory.vxLoader(reflectFactory());
        assertThrows(VxException.class, () -> vxLoader2.getClient(BaseHttpClient.class));
        test.completeNow();
    }

    @Test
    public void testAcceptCharset(Vertx vertx, VertxTestContext test) {
        startMockWebServer1();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));

        val httpClient = vxLoader.getClient(AcceptCharsetHttpClient.class);
        assertEquals(STRING_PREFIX + AcceptCharsetHttpClient.class.getSimpleName() + "@"
                + Integer.toHexString(httpClient.hashCode()), httpClient.toString());
        assertEquals(httpClient, vxLoader.getClient(AcceptCharsetHttpClient.class));
        assertEquals(httpClient.hashCode(), vxLoader.getClient(AcceptCharsetHttpClient.class).hashCode());

        val httpClientNeo = vxLoader.getClient(AcceptCharsetHttpClientNeo.class);
        assertEquals(STRING_PREFIX + AcceptCharsetHttpClientNeo.class.getSimpleName() + "@"
                + Integer.toHexString(httpClientNeo.hashCode()), httpClientNeo.toString());
        assertEquals(httpClientNeo, vxLoader.getClient(AcceptCharsetHttpClientNeo.class));
        assertEquals(httpClientNeo.hashCode(), vxLoader.getClient(AcceptCharsetHttpClientNeo.class).hashCode());

        CompositeFuture.all(newArrayList(
                httpClient.sample().onSuccess(response -> test.verify(() -> assertEquals(ISO_8859_1.name(), response))),
                httpClient.sample2().onSuccess(response -> test.verify(() -> assertEquals(UTF_8.name(), response))),
                httpClient.cover(UTF_8).onSuccess(response -> test.verify(() -> assertEquals(UTF_8.name(), response))),
                httpClientNeo.sample().onSuccess(response -> test.verify(() -> assertEquals(ISO_8859_1.name(), response))),
                httpClientNeo.sample2().onSuccess(response -> test.verify(() -> assertEquals(UTF_8.name(), response)))
        )).onComplete(result -> {
            shutdownMockWebServer1();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    @Test
    public void testContentFormat(Vertx vertx, VertxTestContext test) {
        startMockWebServer2();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));

        val httpClient = vxLoader.getClient(ContentFormatHttpClient.class);
        assertEquals(STRING_PREFIX + ContentFormatHttpClient.class.getSimpleName() + "@"
                + Integer.toHexString(httpClient.hashCode()), httpClient.toString());
        assertEquals(httpClient, vxLoader.getClient(ContentFormatHttpClient.class));
        assertEquals(httpClient.hashCode(), vxLoader.getClient(ContentFormatHttpClient.class).hashCode());

        val httpClientNeo = vxLoader.getClient(ContentFormatHttpClientNeo.class);
        assertEquals(STRING_PREFIX + ContentFormatHttpClientNeo.class.getSimpleName() + "@"
                + Integer.toHexString(httpClientNeo.hashCode()), httpClientNeo.toString());
        assertEquals(httpClientNeo, vxLoader.getClient(ContentFormatHttpClientNeo.class));
        assertEquals(httpClientNeo.hashCode(), vxLoader.getClient(ContentFormatHttpClientNeo.class).hashCode());

        CompositeFuture.all(newArrayList(
                httpClient.sample().onSuccess(response -> test.verify(() -> assertEquals("", response))),
                httpClient.sample2().onSuccess(response -> test.verify(() -> assertEquals("{}", response))),
                httpClient.sample3().onSuccess(response -> test.verify(() -> assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xml/>", response))),
                httpClient.cover(new ContentFormat.ApplicationXmlContentFormatter()).onSuccess(response -> test.verify(() -> assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xml/>", response))),
                httpClientNeo.sample().onSuccess(response -> test.verify(() -> assertEquals("", response))),
                httpClientNeo.sample2().onSuccess(response -> test.verify(() -> assertEquals("{}", response))),
                httpClientNeo.sample3().onSuccess(response -> test.verify(() -> assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xml/>", response)))
        )).onComplete(result -> {
            shutdownMockWebServer2();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    @Test
    public void testRequestMethod(Vertx vertx, VertxTestContext test) {
        startMockWebServer3();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));

        val httpClient = vxLoader.getClient(RequestMethodHttpClient.class);
        assertEquals(STRING_PREFIX + RequestMethodHttpClient.class.getSimpleName() + "@"
                + Integer.toHexString(httpClient.hashCode()), httpClient.toString());
        assertEquals(httpClient, vxLoader.getClient(RequestMethodHttpClient.class));
        assertEquals(httpClient.hashCode(), vxLoader.getClient(RequestMethodHttpClient.class).hashCode());

        val httpClientNeo = vxLoader.getClient(RequestMethodHttpClientNeo.class);
        assertEquals(STRING_PREFIX + RequestMethodHttpClientNeo.class.getSimpleName() + "@"
                + Integer.toHexString(httpClientNeo.hashCode()), httpClientNeo.toString());
        assertEquals(httpClientNeo, vxLoader.getClient(RequestMethodHttpClientNeo.class));
        assertEquals(httpClientNeo.hashCode(), vxLoader.getClient(RequestMethodHttpClientNeo.class).hashCode());

        CompositeFuture.all(newArrayList(
                httpClient.sample().onSuccess(response -> test.verify(() -> assertEquals("POST", response))),
                httpClient.sample2().onSuccess(response -> test.verify(() -> assertEquals("GET", response))),
                httpClient.cover(HttpMethod.GET).onSuccess(response -> test.verify(() -> assertEquals("GET", response))),
                httpClientNeo.sample().onSuccess(response -> test.verify(() -> assertEquals("POST", response))),
                httpClientNeo.sample2().onSuccess(response -> test.verify(() -> assertEquals("GET", response)))
        )).onComplete(result -> {
            shutdownMockWebServer3();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    @Test
    public void testExtendInterface(Vertx vertx, VertxTestContext test) {
        startMockWebServer4();
        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));

        val baseHttpClient = vxLoader.getClient(BaseHttpClient.class);
        assertNotNull(baseHttpClient);

        assertThrows(VxException.class, () -> vxLoader.getClient(SubHttpClient.class));

        shutdownMockWebServer4();
        test.completeNow();
    }

    @AcceptCharset("ISO-8859-1")
    @Mapping("${root}:41130")
    @VxClient
    public interface AcceptCharsetHttpClient {

        Future<String> sample();

        @AcceptCharset("UTF-8")
        Future<String> sample2();

        Future<String> cover(Charset acceptCharset);
    }

    @RequestMethod(HttpMethod.POST)
    @ContentFormat(ContentFormat.FormContentFormatter.class)
    @Mapping("${root}:41131")
    @VxClient
    public interface ContentFormatHttpClient {

        Future<String> sample();

        @ContentFormat(ContentFormat.JsonContentFormatter.class)
        Future<String> sample2();

        @ContentFormat(ContentFormat.ApplicationXmlContentFormatter.class)
        Future<String> sample3();

        Future<String> cover(ContentFormat.ContentFormatter contentFormatter);
    }

    @RequestMethod(HttpMethod.POST)
    @Mapping("${root}:41132")
    @VxClient
    public interface RequestMethodHttpClient {

        Future<String> sample();

        @RequestMethod(HttpMethod.GET)
        Future<String> sample2();

        Future<String> cover(HttpMethod httpMethod);
    }

    @Mapping("${root}:41133")
    @VxClient
    public interface BaseHttpClient {
    }

    public interface SubHttpClient extends BaseHttpClient {
    }

    @Mapping("${root}:41130")
    @VxClient
    @ConfigureWith(AcceptCharsetHttpClientConfig.class)
    public interface AcceptCharsetHttpClientNeo {

        Future<String> sample();

        @ConfigureWith(AcceptCharsetHttpClientSample2Config.class)
        Future<String> sample2();
    }

    @RequestMethod(HttpMethod.POST)
    @Mapping("${root}:41131")
    @VxClient
    @ConfigureWith(ContentFormatHttpClientConfig.class)
    public interface ContentFormatHttpClientNeo {

        Future<String> sample();

        @ConfigureWith(ContentFormatHttpClientSample2Config.class)
        Future<String> sample2();

        @ConfigureWith(ContentFormatHttpClientSample3Config.class)
        Future<String> sample3();
    }

    @Mapping("${root}:41132")
    @VxClient
    @ConfigureWith(RequestMethodHttpClientConfig.class)
    public interface RequestMethodHttpClientNeo {

        Future<String> sample();

        @ConfigureWith(RequestMethodHttpClientSample2Config.class)
        Future<String> sample2();
    }
}
