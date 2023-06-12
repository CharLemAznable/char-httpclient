package com.github.charlemaznable.httpclient.wfclient;

import com.github.charlemaznable.httpclient.annotation.DefaultFallbackDisabled;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonUrlConcatTest;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UrlConcatTest extends CommonUrlConcatTest {

    @Test
    public void testUrlConcat() {
        startMockWebServer();

        val wfLoader = WfFactory.wfLoader(reflectFactory());

        val httpClient = wfLoader.getClient(UrlPlainHttpClient.class);
        assertEquals(ROOT, httpClient.empty().block());
        assertEquals(ROOT, httpClient.root().block());
        assertEquals(SAMPLE, httpClient.sample().block());
        assertEquals(SAMPLE, httpClient.sampleWithSlash().block());
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), httpClient.notFound(WebClient.create()).block());

        shutdownMockWebServer();
    }

    @DefaultFallbackDisabled
    @WfClient
    @Mapping("${root}:41100")
    public interface UrlPlainHttpClient {

        @Mapping
        Mono<String> empty();

        @Mapping("/")
        Mono<String> root();

        Mono<String> sample();

        @Mapping("/sample")
        Mono<String> sampleWithSlash();

        Mono<String> notFound(WebClient client);
    }
}
