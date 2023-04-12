package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.FixedPathVar;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.annotation.PathVar;
import com.github.charlemaznable.httpclient.common.CommonPathVarTest;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathVarTest extends CommonPathVarTest {

    @Test
    public void testPathVar() {
        startMockWebServer();

        val ohLoader = OhFactory.ohLoader(reflectFactory());

        val httpClient = ohLoader.getClient(PathVarHttpClient.class);
        assertEquals("V2", httpClient.sampleDefault());
        assertEquals("V3", httpClient.sampleMapping());
        assertEquals("V4", httpClient.samplePathVars("V4"));

        val httpClientNeo = ohLoader.getClient(PathVarHttpClientNeo.class);
        assertEquals("V2", httpClientNeo.sampleDefault());
        assertEquals("V3", httpClientNeo.sampleMapping());
        assertEquals("V4", httpClientNeo.samplePathVars("V4"));

        shutdownMockWebServer();
    }

    @FixedPathVar(name = "P1", value = "V1")
    @FixedPathVar(name = "P2", value = "V2")
    @Mapping("${root}:41150/{P1}/{P2}")
    @MappingMethodNameDisabled
    @OhClient
    public interface PathVarHttpClient {

        String sampleDefault();

        @FixedPathVar(name = "P2", value = "V3")
        String sampleMapping();

        @FixedPathVar(name = "P2", value = "V3")
        String samplePathVars(@PathVar("P2") String v4);
    }

    @Mapping("${root}:41150/{P1}/{P2}")
    @OhClient
    @ConfigureWith(PathVarHttpClientConfig.class)
    public interface PathVarHttpClientNeo {

        String sampleDefault();

        @ConfigureWith(SampleMappingConfig.class)
        String sampleMapping();

        @ConfigureWith(SampleMappingConfig.class)
        String samplePathVars(@PathVar("P2") String v4);
    }
}
