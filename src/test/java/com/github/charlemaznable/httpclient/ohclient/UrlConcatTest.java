package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.annotation.DefaultFallbackDisabled;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonUrlConcatTest;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import lombok.val;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UrlConcatTest extends CommonUrlConcatTest {

    @Test
    public void testUrlConcat() {
        startMockWebServer();

        val ohLoader = OhFactory.ohLoader(reflectFactory());

        val httpClient = ohLoader.getClient(UrlPlainHttpClient.class);
        assertEquals(ROOT, httpClient.empty());
        assertEquals(ROOT, httpClient.root());
        assertEquals(SAMPLE, httpClient.sample());
        assertEquals(SAMPLE, httpClient.sampleWithSlash());
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), httpClient.notFound(new OkHttpClient()));

        shutdownMockWebServer();
    }

    @DefaultFallbackDisabled
    @OhClient
    @Mapping("${root}:41100")
    public interface UrlPlainHttpClient {

        @Mapping
        String empty();

        @Mapping("/")
        String root();

        String sample();

        @Mapping("/sample")
        String sampleWithSlash();

        String notFound(OkHttpClient client);
    }
}
