package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.httpclient.annotation.DefaultFallbackDisabled;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonUrlConcatTest;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class UrlConcatTest extends CommonUrlConcatTest {

    @Test
    public void testUrlConcat(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));

        val httpClient = vxLoader.getClient(UrlPlainHttpClient.class);

        CompositeFuture.all(newArrayList(
                httpClient.empty().onSuccess(response -> test.verify(() -> assertEquals(ROOT, response))),
                httpClient.root().onSuccess(response -> test.verify(() -> assertEquals(ROOT, response))),
                httpClient.sample().onSuccess(response -> test.verify(() -> assertEquals(SAMPLE, response))),
                httpClient.sampleWithSlash().onSuccess(response -> test.verify(() -> assertEquals(SAMPLE, response))),
                httpClient.notFound(WebClient.create(vertx)).onSuccess(response -> test.verify(() -> assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), response)))
        )).onComplete(result -> {
            shutdownMockWebServer();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    @DefaultFallbackDisabled
    @VxClient
    @Mapping("${root}:41100")
    public interface UrlPlainHttpClient {

        @Mapping
        Future<String> empty();

        @Mapping("/")
        Future<String> root();

        Future<String> sample();

        @Mapping("/sample")
        Future<String> sampleWithSlash();

        Future<String> notFound(WebClient client);
    }
}
