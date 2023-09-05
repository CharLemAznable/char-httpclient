package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.FixedHeader;
import com.github.charlemaznable.httpclient.annotation.Header;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonHeaderTest;
import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class HeaderTest extends CommonHeaderTest {

    @Test
    public void testHeader(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));

        val httpClient = vxLoader.getClient(HeaderHttpClient.class);
        val httpClientNeo = vxLoader.getClient(HeaderHttpClientNeo.class);

        Future.all(newArrayList(
                httpClient.sampleDefault().onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClient.sampleMapping().onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClient.sampleHeaders(null, "V4").onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClientNeo.sampleDefault().onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClientNeo.sampleMapping().onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClientNeo.sampleHeaders(null, "V4").onSuccess(response -> test.verify(() -> assertEquals("OK", response)))
        )).onComplete(result -> {
            shutdownMockWebServer();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    @FixedHeader(name = "H1", value = "V1")
    @FixedHeader(name = "H2", value = "V2")
    @Mapping("${root}:41140")
    @VxClient
    public interface HeaderHttpClient {

        Future<String> sampleDefault();

        @FixedHeader(name = "H2")
        @FixedHeader(name = "H3", value = "V3")
        Future<String> sampleMapping();

        @FixedHeader(name = "H2")
        @FixedHeader(name = "H3", value = "V3")
        Future<String> sampleHeaders(@Header("H3") String v3,
                                     @Header("H4") String v4);
    }

    @Mapping("${root}:41140")
    @VxClient
    @ConfigureWith(HeaderHttpClientConfig.class)
    public interface HeaderHttpClientNeo {

        Future<String> sampleDefault();

        @ConfigureWith(SampleMappingConfig.class)
        Future<String> sampleMapping();

        @ConfigureWith(SampleMappingConfig.class)
        Future<String> sampleHeaders(@Header("H3") String v3,
                                     @Header("H4") String v4);
    }
}
