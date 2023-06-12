package com.github.charlemaznable.httpclient.wfclient;

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
import lombok.val;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Mapp.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParameterTest extends CommonParameterTest {

    @Test
    public void testParameterGet() {
        startGetMockWebServer();

        val wfLoader = WfFactory.wfLoader(reflectFactory());

        val httpClient = wfLoader.getClient(GetParameterHttpClient.class);
        assertEquals("OK", httpClient.sampleDefault().block());
        assertEquals("OK", httpClient.sampleMapping().block());
        assertEquals("OK", httpClient.sampleParameters(null, "V4").block());
        assertEquals("OK", httpClient.sampleBundle(new TestBundle(null, null, "V4", "V5")).block());
        assertEquals("OK", httpClient.sampleBundle2(null).block());
        assertEquals("OK", httpClient.sampleBundle3(of("T2", null, "T4", "V4", "t5", "V5")).block());

        val httpClientNeo = wfLoader.getClient(GetParameterHttpClientNeo.class);
        assertEquals("OK", httpClientNeo.sampleDefault().block());
        assertEquals("OK", httpClientNeo.sampleMapping().block());
        assertEquals("OK", httpClientNeo.sampleParameters(null, "V4").block());
        assertEquals("OK", httpClientNeo.sampleBundle(new TestBundle(null, null, "V4", "V5")).block());
        assertEquals("OK", httpClientNeo.sampleBundle2(null).block());
        assertEquals("OK", httpClientNeo.sampleBundle3(of("T2", null, "T4", "V4", "t5", "V5")).block());

        shutdownGetMockWebServer();
    }

    @Test
    public void testParameterPost() {
        startPostMockWebServer();

        val wfLoader = WfFactory.wfLoader(reflectFactory());

        val httpClient = wfLoader.getClient(PostParameterHttpClient.class);
        assertEquals("OK", httpClient.sampleDefault().block());
        assertEquals("OK", httpClient.sampleMapping().block());
        assertEquals("OK", httpClient.sampleParameters(null, "V4").block());
        assertEquals("OK", httpClient.sampleBundle(new TestBundle(null, null, "V4", "V5")).block());
        assertEquals("OK", httpClient.sampleBundle2(null).block());
        assertEquals("OK", httpClient.sampleBundle3(of("T2", null, "T4", "V4", "t5", "V5")).block());
        assertEquals("OK", httpClient.sampleRaw("T3=V3&T4=V4").block());
        assertEquals("OK", httpClient.sampleRawError(new Object()).block());

        val httpClientNeo = wfLoader.getClient(PostParameterHttpClientNeo.class);
        assertEquals("OK", httpClientNeo.sampleDefault().block());
        assertEquals("OK", httpClientNeo.sampleMapping().block());
        assertEquals("OK", httpClientNeo.sampleParameters(null, "V4").block());
        assertEquals("OK", httpClientNeo.sampleBundle(new TestBundle(null, null, "V4", "V5")).block());
        assertEquals("OK", httpClientNeo.sampleBundle2(null).block());
        assertEquals("OK", httpClientNeo.sampleBundle3(of("T2", null, "T4", "V4", "t5", "V5")).block());
        assertEquals("OK", httpClientNeo.sampleRaw("T3=V3&T4=V4").block());
        assertEquals("OK", httpClientNeo.sampleRawError(new Object()).block());

        shutdownPostMockWebServer();
    }

    @FixedPathVar(name = "T0", value = "V0")
    @FixedParameter(name = "T1", value = "V1")
    @FixedParameter(name = "T2", value = "V2")
    @Mapping("${root}:41160")
    @WfClient
    public interface GetParameterHttpClient {

        Mono<String> sampleDefault();

        @Mapping("/sampleMapping?T0={T0}")
        @FixedParameter(name = "T2")
        @FixedParameter(name = "T3", value = "V3")
        Mono<String> sampleMapping();

        @Mapping("/sampleParameters?T0={T0}")
        @FixedParameter(name = "T2")
        @FixedParameter(name = "T3", value = "V3")
        Mono<String> sampleParameters(@Parameter("T3") String v3,
                                      @Parameter("T4") String v4);

        Mono<String> sampleBundle(@Bundle TestBundle bundle);

        Mono<String> sampleBundle2(@Bundle TestBundle bundle);

        Mono<String> sampleBundle3(@Bundle Map<String, Object> bundle);
    }

    @FixedParameter(name = "T1", value = "V1")
    @FixedParameter(name = "T2", value = "V2")
    @RequestMethod(HttpMethod.POST)
    @ContentFormat(ContentFormat.FormContentFormatter.class)
    @Mapping("${root}:41161")
    @WfClient
    public interface PostParameterHttpClient {

        Mono<String> sampleDefault();

        @ContentFormat(ContentFormat.JsonContentFormatter.class)
        @FixedParameter(name = "T2")
        @FixedParameter(name = "T3", value = "V3")
        Mono<String> sampleMapping();

        @ContentFormat(ContentFormat.TextXmlContentFormatter.class)
        @FixedParameter(name = "T2")
        @FixedParameter(name = "T3", value = "V3")
        Mono<String> sampleParameters(@Parameter("T3") String v3,
                                      @Parameter("T4") String v4);

        Mono<String> sampleBundle(@Bundle TestBundle bundle);

        Mono<String> sampleBundle2(@Bundle TestBundle bundle);

        Mono<String> sampleBundle3(@Bundle Map<String, Object> bundle);

        Mono<String> sampleRaw(@RequestBodyRaw String raw);

        Mono<String> sampleRawError(@RequestBodyRaw Object raw);
    }

    @FixedPathVar(name = "T0", value = "V0")
    @Mapping("${root}:41160")
    @WfClient
    @ConfigureWith(ParameterHttpClientConfig.class)
    public interface GetParameterHttpClientNeo {

        Mono<String> sampleDefault();

        @Mapping("/sampleMapping?T0={T0}")
        @ConfigureWith(SampleMappingConfig.class)
        Mono<String> sampleMapping();

        @Mapping("/sampleParameters?T0={T0}")
        @ConfigureWith(SampleMappingConfig.class)
        Mono<String> sampleParameters(@Parameter("T3") String v3,
                                      @Parameter("T4") String v4);

        Mono<String> sampleBundle(@Bundle TestBundle bundle);

        Mono<String> sampleBundle2(@Bundle TestBundle bundle);

        Mono<String> sampleBundle3(@Bundle Map<String, Object> bundle);
    }

    @RequestMethod(HttpMethod.POST)
    @Mapping("${root}:41161")
    @WfClient
    @ConfigureWith(ParameterHttpClientConfig.class)
    public interface PostParameterHttpClientNeo {

        Mono<String> sampleDefault();

        @ConfigureWith(SampleMappingConfig.class)
        Mono<String> sampleMapping();

        @ConfigureWith(SampleParametersConfig.class)
        Mono<String> sampleParameters(@Parameter("T3") String v3,
                                      @Parameter("T4") String v4);

        Mono<String> sampleBundle(@Bundle TestBundle bundle);

        Mono<String> sampleBundle2(@Bundle TestBundle bundle);

        Mono<String> sampleBundle3(@Bundle Map<String, Object> bundle);

        Mono<String> sampleRaw(@RequestBodyRaw String raw);

        Mono<String> sampleRawError(@RequestBodyRaw Object raw);
    }
}
