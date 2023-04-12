package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.FixedPathVar;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.annotation.PathVar;
import com.github.charlemaznable.httpclient.common.CommonPathVarTest;
import com.github.charlemaznable.httpclient.common.VertxReflectFactory;
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
public class PathVarTest extends CommonPathVarTest {

    @Test
    public void testPathVar(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));

        val httpClient = vxLoader.getClient(PathVarHttpClient.class);
        val httpClientNeo = vxLoader.getClient(PathVarHttpClientNeo.class);

        CompositeFuture.all(newArrayList(
                httpClient.sampleDefault().onSuccess(response -> test.verify(() -> assertEquals("V2", response))),
                httpClient.sampleMapping().onSuccess(response -> test.verify(() -> assertEquals("V3", response))),
                httpClient.samplePathVars("V4").onSuccess(response -> test.verify(() -> assertEquals("V4", response))),
                httpClientNeo.sampleDefault().onSuccess(response -> test.verify(() -> assertEquals("V2", response))),
                httpClientNeo.sampleMapping().onSuccess(response -> test.verify(() -> assertEquals("V3", response))),
                httpClientNeo.samplePathVars("V4").onSuccess(response -> test.verify(() -> assertEquals("V4", response)))
        )).onComplete(result -> {
            shutdownMockWebServer();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    @FixedPathVar(name = "P1", value = "V1")
    @FixedPathVar(name = "P2", value = "V2")
    @Mapping("${root}:41150/{P1}/{P2}")
    @MappingMethodNameDisabled
    @VxClient
    public interface PathVarHttpClient {

        Future<String> sampleDefault();

        @FixedPathVar(name = "P2", value = "V3")
        Future<String> sampleMapping();

        @FixedPathVar(name = "P2", value = "V3")
        Future<String> samplePathVars(@PathVar("P2") String v4);
    }

    @Mapping("${root}:41150/{P1}/{P2}")
    @VxClient
    @ConfigureWith(PathVarHttpClientConfig.class)
    public interface PathVarHttpClientNeo {

        Future<String> sampleDefault();

        @ConfigureWith(SampleMappingConfig.class)
        Future<String> sampleMapping();

        @ConfigureWith(SampleMappingConfig.class)
        Future<String> samplePathVars(@PathVar("P2") String v4);
    }
}
