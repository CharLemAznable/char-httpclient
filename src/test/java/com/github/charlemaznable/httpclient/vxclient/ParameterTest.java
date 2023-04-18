package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.httpclient.annotation.Bundle;
import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.ContentFormat;
import com.github.charlemaznable.httpclient.annotation.FixedParameter;
import com.github.charlemaznable.httpclient.annotation.FixedPathVar;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.Parameter;
import com.github.charlemaznable.httpclient.annotation.RequestBodyRaw;
import com.github.charlemaznable.httpclient.annotation.RequestMethod;
import com.github.charlemaznable.httpclient.common.CommonParameterTest;
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

import java.util.Map;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class ParameterTest extends CommonParameterTest {

    @Test
    public void testParameterGet(Vertx vertx, VertxTestContext test) {
        startGetMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));

        val httpClient = vxLoader.getClient(GetParameterHttpClient.class);
        val httpClientNeo = vxLoader.getClient(GetParameterHttpClientNeo.class);

        CompositeFuture.all(newArrayList(
                httpClient.sampleDefault().onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClient.sampleMapping().onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClient.sampleParameters(null, "V4").onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClient.sampleBundle(new TestBundle(null, null, "V4", "V5")).onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClient.sampleBundle2(null).onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClient.sampleBundle3(of("T2", null, "T4", "V4", "t5", "V5")).onSuccess(response -> test.verify(() -> assertEquals("OK", response))),

                httpClientNeo.sampleDefault().onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClientNeo.sampleMapping().onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClientNeo.sampleParameters(null, "V4").onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClientNeo.sampleBundle(new TestBundle(null, null, "V4", "V5")).onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClientNeo.sampleBundle2(null).onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClientNeo.sampleBundle3(of("T2", null, "T4", "V4", "t5", "V5")).onSuccess(response -> test.verify(() -> assertEquals("OK", response)))
        )).onComplete(result -> {
            shutdownGetMockWebServer();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    @Test
    public void testParameterPost(Vertx vertx, VertxTestContext test) {
        startPostMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));

        val httpClient = vxLoader.getClient(PostParameterHttpClient.class);
        val httpClientNeo = vxLoader.getClient(PostParameterHttpClientNeo.class);

        CompositeFuture.all(newArrayList(
                httpClient.sampleDefault().onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClient.sampleMapping().onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClient.sampleParameters(null, "V4").onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClient.sampleBundle(new TestBundle(null, null, "V4", "V5")).onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClient.sampleBundle2(null).onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClient.sampleBundle3(of("T2", null, "T4", "V4", "t5", "V5")).onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClient.sampleRaw("T3=V3&T4=V4").onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClient.sampleRawError(new Object()).onSuccess(response -> test.verify(() -> assertEquals("OK", response))),

                httpClientNeo.sampleDefault().onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClientNeo.sampleMapping().onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClientNeo.sampleParameters(null, "V4").onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClientNeo.sampleBundle(new TestBundle(null, null, "V4", "V5")).onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClientNeo.sampleBundle2(null).onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClientNeo.sampleBundle3(of("T2", null, "T4", "V4", "t5", "V5")).onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClientNeo.sampleRaw("T3=V3&T4=V4").onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClientNeo.sampleRawError(new Object()).onSuccess(response -> test.verify(() -> assertEquals("OK", response)))
        )).onComplete(result -> {
            shutdownPostMockWebServer();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    @FixedPathVar(name = "T0", value = "V0")
    @FixedParameter(name = "T1", value = "V1")
    @FixedParameter(name = "T2", value = "V2")
    @Mapping("${root}:41160")
    @VxClient
    public interface GetParameterHttpClient {

        Future<String> sampleDefault();

        @Mapping("/sampleMapping?T0={T0}")
        @FixedParameter(name = "T2")
        @FixedParameter(name = "T3", value = "V3")
        Future<String> sampleMapping();

        @Mapping("/sampleParameters?T0={T0}")
        @FixedParameter(name = "T2")
        @FixedParameter(name = "T3", value = "V3")
        Future<String> sampleParameters(@Parameter("T3") String v3,
                                        @Parameter("T4") String v4);

        Future<String> sampleBundle(@Bundle TestBundle bundle);

        Future<String> sampleBundle2(@Bundle TestBundle bundle);

        Future<String> sampleBundle3(@Bundle Map<String, Object> bundle);
    }

    @FixedParameter(name = "T1", value = "V1")
    @FixedParameter(name = "T2", value = "V2")
    @RequestMethod(HttpMethod.POST)
    @ContentFormat(ContentFormat.FormContentFormatter.class)
    @Mapping("${root}:41161")
    @VxClient
    public interface PostParameterHttpClient {

        Future<String> sampleDefault();

        @ContentFormat(ContentFormat.JsonContentFormatter.class)
        @FixedParameter(name = "T2")
        @FixedParameter(name = "T3", value = "V3")
        Future<String> sampleMapping();

        @ContentFormat(ContentFormat.TextXmlContentFormatter.class)
        @FixedParameter(name = "T2")
        @FixedParameter(name = "T3", value = "V3")
        Future<String> sampleParameters(@Parameter("T3") String v3,
                                        @Parameter("T4") String v4);

        Future<String> sampleBundle(@Bundle TestBundle bundle);

        Future<String> sampleBundle2(@Bundle TestBundle bundle);

        Future<String> sampleBundle3(@Bundle Map<String, Object> bundle);

        Future<String> sampleRaw(@RequestBodyRaw String raw);

        Future<String> sampleRawError(@RequestBodyRaw Object raw);
    }

    @FixedPathVar(name = "T0", value = "V0")
    @Mapping("${root}:41160")
    @VxClient
    @ConfigureWith(ParameterHttpClientConfig.class)
    public interface GetParameterHttpClientNeo {

        Future<String> sampleDefault();

        @Mapping("/sampleMapping?T0={T0}")
        @ConfigureWith(SampleMappingConfig.class)
        Future<String> sampleMapping();

        @Mapping("/sampleParameters?T0={T0}")
        @ConfigureWith(SampleMappingConfig.class)
        Future<String> sampleParameters(@Parameter("T3") String v3,
                                        @Parameter("T4") String v4);

        Future<String> sampleBundle(@Bundle TestBundle bundle);

        Future<String> sampleBundle2(@Bundle TestBundle bundle);

        Future<String> sampleBundle3(@Bundle Map<String, Object> bundle);
    }

    @RequestMethod(HttpMethod.POST)
    @Mapping("${root}:41161")
    @VxClient
    @ConfigureWith(ParameterHttpClientConfig.class)
    public interface PostParameterHttpClientNeo {

        Future<String> sampleDefault();

        @ConfigureWith(SampleMappingConfig.class)
        Future<String> sampleMapping();

        @ConfigureWith(SampleParametersConfig.class)
        Future<String> sampleParameters(@Parameter("T3") String v3,
                                        @Parameter("T4") String v4);

        Future<String> sampleBundle(@Bundle TestBundle bundle);

        Future<String> sampleBundle2(@Bundle TestBundle bundle);

        Future<String> sampleBundle3(@Bundle Map<String, Object> bundle);

        Future<String> sampleRaw(@RequestBodyRaw String raw);

        Future<String> sampleRawError(@RequestBodyRaw Object raw);
    }
}
