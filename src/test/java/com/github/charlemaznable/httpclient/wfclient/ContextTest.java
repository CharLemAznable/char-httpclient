package com.github.charlemaznable.httpclient.wfclient;

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
import lombok.val;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContextTest extends CommonContextTest {

    @Test
    public void testContext() {
        startMockWebServer();

        val wfLoader = WfFactory.wfLoader(reflectFactory());

        val httpClient = wfLoader.getClient(ContextHttpClient.class);
        assertEquals("OK", httpClient.sampleDefault().block());
        assertEquals("OK", httpClient.sampleMapping().block());
        assertEquals("OK", httpClient.sampleContexts(null, "V4").block());
        assertEquals("OK", requireNonNull(httpClient.sampleDefaultResponse().block()).getResponse());
        assertEquals("OK", requireNonNull(httpClient.sampleDefaultResponseCover(new TestRequestExtender(), new TestResponseParser()).block()).getResponse());
        assertEquals("OK", requireNonNull(httpClient.sampleMappingResponse().block()).getResponse());
        assertEquals("OK", requireNonNull(httpClient.sampleContextsResponse(null, "V4").block()).getResponse());
        assertEquals("OK", httpClient.sampleNone().block());

        val httpClientNeo = wfLoader.getClient(ContextHttpClientNeo.class);
        assertEquals("OK", httpClientNeo.sampleDefault().block());
        assertEquals("OK", httpClientNeo.sampleMapping().block());
        assertEquals("OK", httpClientNeo.sampleContexts(null, "V4").block());
        assertEquals("OK", requireNonNull(httpClientNeo.sampleDefaultResponse().block()).getResponse());
        assertEquals("OK", requireNonNull(httpClientNeo.sampleMappingResponse().block()).getResponse());
        assertEquals("OK", requireNonNull(httpClientNeo.sampleContextsResponse(null, "V4").block()).getResponse());
        assertEquals("OK", httpClientNeo.sampleNone().block());

        shutdownMockWebServer();
    }

    @RequestMethod(HttpMethod.POST)
    @ContentFormat(TestContextFormatter.class)
    @ResponseParse(TestResponseParser.class)
    @RequestExtend(TestRequestExtender.class)
    @FixedContext(name = "C1", value = "V1")
    @FixedContext(name = "C2", value = "V2")
    @Mapping("${root}:41170")
    @WfClient
    public interface ContextHttpClient {

        Mono<String> sampleDefault();

        @FixedContext(name = "C2")
        @FixedContext(name = "C3", value = "V3")
        Mono<String> sampleMapping();

        @FixedContext(name = "C2")
        @FixedContext(name = "C3", value = "V3")
        Mono<String> sampleContexts(@Context("C3") String v3,
                                    @Context("C4") String v4);

        @ResponseParse(TestResponseParser.class)
        @RequestExtend(TestRequestExtender.class)
        @Mapping("/sampleDefault")
        Mono<TestResponse> sampleDefaultResponse();

        @Mapping("/sampleDefault")
        Mono<TestResponse> sampleDefaultResponseCover(RequestExtend.RequestExtender requestExtender,
                                                      ResponseParse.ResponseParser responseParser);

        @ResponseParse(TestResponseParser.class)
        @RequestExtend(TestRequestExtender.class)
        @Mapping("/sampleMapping")
        @FixedContext(name = "C2")
        @FixedContext(name = "C3", value = "V3")
        Mono<TestResponse> sampleMappingResponse();

        @ResponseParse(TestResponseParser.class)
        @RequestExtend(TestRequestExtender.class)
        @Mapping("/sampleContexts")
        @FixedContext(name = "C2")
        @FixedContext(name = "C3", value = "V3")
        Mono<TestResponse> sampleContextsResponse(@Context("C3") String v3,
                                                  @Context("C4") String v4);

        @ResponseParse.Disabled
        @RequestExtend.Disabled
        Mono<String> sampleNone();
    }

    @WfClient
    @ConfigureWith(ContextHttpClientConfig.class)
    public interface ContextHttpClientNeo {

        Mono<String> sampleDefault();

        @ConfigureWith(SampleMappingConfig.class)
        Mono<String> sampleMapping();

        @ConfigureWith(SampleMappingConfig.class)
        Mono<String> sampleContexts(@Context("C3") String v3,
                                    @Context("C4") String v4);

        @ResponseParse(TestResponseParser.class)
        @RequestExtend(TestRequestExtender.class)
        @Mapping("/sampleDefault")
        Mono<TestResponse> sampleDefaultResponse();

        @Mapping("/sampleMapping")
        @ConfigureWith(SampleMappingConfig2.class)
        Mono<TestResponse> sampleMappingResponse();

        @Mapping("/sampleContexts")
        @ConfigureWith(SampleMappingConfig2.class)
        Mono<TestResponse> sampleContextsResponse(@Context("C3") String v3,
                                                  @Context("C4") String v4);

        @ConfigureWith(SampleMappingConfig3.class)
        Mono<String> sampleNone();
    }
}
