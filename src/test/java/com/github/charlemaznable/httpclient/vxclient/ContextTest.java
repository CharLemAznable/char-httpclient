package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.ContentFormat;
import com.github.charlemaznable.httpclient.annotation.Context;
import com.github.charlemaznable.httpclient.annotation.FixedContext;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.RequestExtend;
import com.github.charlemaznable.httpclient.annotation.RequestMethod;
import com.github.charlemaznable.httpclient.annotation.ResponseParse;
import com.github.charlemaznable.httpclient.common.CommonContextTest;
import com.github.charlemaznable.httpclient.common.HttpMethod;
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
public class ContextTest extends CommonContextTest {

    @Test
    public void testContext(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));

        val httpClient = vxLoader.getClient(ContextHttpClient.class);
        val httpClientNeo = vxLoader.getClient(ContextHttpClientNeo.class);

        CompositeFuture.all(newArrayList(
                httpClient.sampleDefault().onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClient.sampleMapping().onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClient.sampleContexts(null, "V4").onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClient.sampleDefaultResponse().onSuccess(response -> test.verify(() -> assertEquals("OK", response.getResponse()))),
                httpClient.sampleDefaultResponseCover(new TestRequestExtender(), new TestResponseParser()).onSuccess(response -> test.verify(() -> assertEquals("OK", response.getResponse()))),
                httpClient.sampleMappingResponse().onSuccess(response -> test.verify(() -> assertEquals("OK", response.getResponse()))),
                httpClient.sampleContextsResponse(null, "V4").onSuccess(response -> test.verify(() -> assertEquals("OK", response.getResponse()))),
                httpClient.sampleNone().onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClientNeo.sampleDefault().onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClientNeo.sampleMapping().onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClientNeo.sampleContexts(null, "V4").onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClientNeo.sampleDefaultResponse().onSuccess(response -> test.verify(() -> assertEquals("OK", response.getResponse()))),
                httpClientNeo.sampleMappingResponse().onSuccess(response -> test.verify(() -> assertEquals("OK", response.getResponse()))),
                httpClientNeo.sampleContextsResponse(null, "V4").onSuccess(response -> test.verify(() -> assertEquals("OK", response.getResponse()))),
                httpClientNeo.sampleNone().onSuccess(response -> test.verify(() -> assertEquals("OK", response)))
        )).onComplete(result -> {
            shutdownMockWebServer();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    @RequestMethod(HttpMethod.POST)
    @ContentFormat(TestContextFormatter.class)
    @ResponseParse(TestResponseParser.class)
    @RequestExtend(TestRequestExtender.class)
    @FixedContext(name = "C1", value = "V1")
    @FixedContext(name = "C2", value = "V2")
    @Mapping("${root}:41170")
    @VxClient
    public interface ContextHttpClient {

        Future<String> sampleDefault();

        @FixedContext(name = "C2")
        @FixedContext(name = "C3", value = "V3")
        Future<String> sampleMapping();

        @FixedContext(name = "C2")
        @FixedContext(name = "C3", value = "V3")
        Future<String> sampleContexts(@Context("C3") String v3,
                                      @Context("C4") String v4);

        @ResponseParse(TestResponseParser.class)
        @RequestExtend(TestRequestExtender.class)
        @Mapping("/sampleDefault")
        Future<TestResponse> sampleDefaultResponse();

        @Mapping("/sampleDefault")
        Future<TestResponse> sampleDefaultResponseCover(RequestExtend.RequestExtender requestExtender,
                                                        ResponseParse.ResponseParser responseParser);

        @ResponseParse(TestResponseParser.class)
        @RequestExtend(TestRequestExtender.class)
        @Mapping("/sampleMapping")
        @FixedContext(name = "C2")
        @FixedContext(name = "C3", value = "V3")
        Future<TestResponse> sampleMappingResponse();

        @ResponseParse(TestResponseParser.class)
        @RequestExtend(TestRequestExtender.class)
        @Mapping("/sampleContexts")
        @FixedContext(name = "C2")
        @FixedContext(name = "C3", value = "V3")
        Future<TestResponse> sampleContextsResponse(@Context("C3") String v3,
                                                    @Context("C4") String v4);

        @ResponseParse.Disabled
        @RequestExtend.Disabled
        Future<String> sampleNone();
    }

    @VxClient
    @ConfigureWith(ContextHttpClientConfig.class)
    public interface ContextHttpClientNeo {

        Future<String> sampleDefault();

        @ConfigureWith(SampleMappingConfig.class)
        Future<String> sampleMapping();

        @ConfigureWith(SampleMappingConfig.class)
        Future<String> sampleContexts(@Context("C3") String v3,
                                      @Context("C4") String v4);

        @ResponseParse(TestResponseParser.class)
        @RequestExtend(TestRequestExtender.class)
        @Mapping("/sampleDefault")
        Future<TestResponse> sampleDefaultResponse();

        @Mapping("/sampleMapping")
        @ConfigureWith(SampleMappingConfig2.class)
        Future<TestResponse> sampleMappingResponse();

        @Mapping("/sampleContexts")
        @ConfigureWith(SampleMappingConfig2.class)
        Future<TestResponse> sampleContextsResponse(@Context("C3") String v3,
                                                    @Context("C4") String v4);

        @ConfigureWith(SampleMappingConfig3.class)
        Future<String> sampleNone();
    }
}
