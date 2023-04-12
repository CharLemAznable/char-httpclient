package com.github.charlemaznable.httpclient.ohclient;

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

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContextTest extends CommonContextTest {

    @Test
    public void testContext() {
        startMockWebServer();

        val ohLoader = OhFactory.ohLoader(reflectFactory());

        val httpClient = ohLoader.getClient(ContextHttpClient.class);
        assertEquals("OK", httpClient.sampleDefault());
        assertEquals("OK", httpClient.sampleMapping());
        assertEquals("OK", httpClient.sampleContexts(null, "V4"));
        assertEquals("OK", httpClient.sampleDefaultResponse().getResponse());
        assertEquals("OK", httpClient.sampleDefaultResponseCover(new TestRequestExtender(), new TestResponseParser()).getResponse());
        assertEquals("OK", httpClient.sampleMappingResponse().getResponse());
        assertEquals("OK", httpClient.sampleContextsResponse(null, "V4").getResponse());
        assertEquals("OK", httpClient.sampleNone());

        val httpClientNeo = ohLoader.getClient(ContextHttpClientNeo.class);
        assertEquals("OK", httpClientNeo.sampleDefault());
        assertEquals("OK", httpClientNeo.sampleMapping());
        assertEquals("OK", httpClientNeo.sampleContexts(null, "V4"));
        assertEquals("OK", httpClientNeo.sampleDefaultResponse().getResponse());
        assertEquals("OK", httpClientNeo.sampleMappingResponse().getResponse());
        assertEquals("OK", httpClientNeo.sampleContextsResponse(null, "V4").getResponse());
        assertEquals("OK", httpClientNeo.sampleNone());

        shutdownMockWebServer();
    }

    @RequestMethod(HttpMethod.POST)
    @ContentFormat(TestContextFormatter.class)
    @ResponseParse(TestResponseParser.class)
    @RequestExtend(TestRequestExtender.class)
    @FixedContext(name = "C1", value = "V1")
    @FixedContext(name = "C2", value = "V2")
    @Mapping("${root}:41170")
    @OhClient
    public interface ContextHttpClient {

        String sampleDefault();

        @FixedContext(name = "C2")
        @FixedContext(name = "C3", value = "V3")
        String sampleMapping();

        @FixedContext(name = "C2")
        @FixedContext(name = "C3", value = "V3")
        String sampleContexts(@Context("C3") String v3,
                              @Context("C4") String v4);

        @ResponseParse(TestResponseParser.class)
        @RequestExtend(TestRequestExtender.class)
        @Mapping("/sampleDefault")
        TestResponse sampleDefaultResponse();

        @Mapping("/sampleDefault")
        TestResponse sampleDefaultResponseCover(RequestExtend.RequestExtender requestExtender,
                                                ResponseParse.ResponseParser responseParser);

        @ResponseParse(TestResponseParser.class)
        @RequestExtend(TestRequestExtender.class)
        @Mapping("/sampleMapping")
        @FixedContext(name = "C2")
        @FixedContext(name = "C3", value = "V3")
        TestResponse sampleMappingResponse();

        @ResponseParse(TestResponseParser.class)
        @RequestExtend(TestRequestExtender.class)
        @Mapping("/sampleContexts")
        @FixedContext(name = "C2")
        @FixedContext(name = "C3", value = "V3")
        TestResponse sampleContextsResponse(@Context("C3") String v3,
                                            @Context("C4") String v4);

        @ResponseParse.Disabled
        @RequestExtend.Disabled
        String sampleNone();
    }

    @OhClient
    @ConfigureWith(ContextHttpClientConfig.class)
    public interface ContextHttpClientNeo {

        String sampleDefault();

        @ConfigureWith(SampleMappingConfig.class)
        String sampleMapping();

        @ConfigureWith(SampleMappingConfig.class)
        String sampleContexts(@Context("C3") String v3,
                              @Context("C4") String v4);

        @ResponseParse(TestResponseParser.class)
        @RequestExtend(TestRequestExtender.class)
        @Mapping("/sampleDefault")
        TestResponse sampleDefaultResponse();

        @Mapping("/sampleMapping")
        @ConfigureWith(SampleMappingConfig2.class)
        TestResponse sampleMappingResponse();

        @Mapping("/sampleContexts")
        @ConfigureWith(SampleMappingConfig2.class)
        TestResponse sampleContextsResponse(@Context("C3") String v3,
                                            @Context("C4") String v4);

        @ConfigureWith(SampleMappingConfig3.class)
        String sampleNone();
    }
}
