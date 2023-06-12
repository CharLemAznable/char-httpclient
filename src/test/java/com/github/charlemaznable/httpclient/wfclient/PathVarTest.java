package com.github.charlemaznable.httpclient.wfclient;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.FixedPathVar;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.annotation.PathVar;
import com.github.charlemaznable.httpclient.common.CommonPathVarTest;
import lombok.val;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathVarTest extends CommonPathVarTest {

    @Test
    public void testPathVar() {
        startMockWebServer();

        val wfLoader = WfFactory.wfLoader(reflectFactory());

        val httpClient = wfLoader.getClient(PathVarHttpClient.class);
        assertEquals("V2", httpClient.sampleDefault().block());
        assertEquals("V3", httpClient.sampleMapping().block());
        assertEquals("V4", httpClient.samplePathVars("V4").block());

        val httpClientNeo = wfLoader.getClient(PathVarHttpClientNeo.class);
        assertEquals("V2", httpClientNeo.sampleDefault().block());
        assertEquals("V3", httpClientNeo.sampleMapping().block());
        assertEquals("V4", httpClientNeo.samplePathVars("V4").block());

        shutdownMockWebServer();
    }

    @FixedPathVar(name = "P1", value = "V1")
    @FixedPathVar(name = "P2", value = "V2")
    @Mapping("${root}:41150/{P1}/{P2}")
    @MappingMethodNameDisabled
    @WfClient
    public interface PathVarHttpClient {

        Mono<String> sampleDefault();

        @FixedPathVar(name = "P2", value = "V3")
        Mono<String> sampleMapping();

        @FixedPathVar(name = "P2", value = "V3")
        Mono<String> samplePathVars(@PathVar("P2") String v4);
    }

    @Mapping("${root}:41150/{P1}/{P2}")
    @WfClient
    @ConfigureWith(PathVarHttpClientConfig.class)
    public interface PathVarHttpClientNeo {

        Mono<String> sampleDefault();

        @ConfigureWith(SampleMappingConfig.class)
        Mono<String> sampleMapping();

        @ConfigureWith(SampleMappingConfig.class)
        Mono<String> samplePathVars(@PathVar("P2") String v4);
    }
}
