package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.FixedHeader;
import com.github.charlemaznable.httpclient.annotation.Header;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonHeaderTest;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HeaderTest extends CommonHeaderTest {

    @Test
    public void testHeader() {
        startMockWebServer();

        val ohLoader = OhFactory.ohLoader(reflectFactory());

        val httpClient = ohLoader.getClient(HeaderHttpClient.class);
        assertEquals("OK", httpClient.sampleDefault());
        assertEquals("OK", httpClient.sampleMapping());
        assertEquals("OK", httpClient.sampleHeaders(null, "V4"));

        val httpClientNeo = ohLoader.getClient(HeaderHttpClientNeo.class);
        assertEquals("OK", httpClientNeo.sampleDefault());
        assertEquals("OK", httpClientNeo.sampleMapping());
        assertEquals("OK", httpClientNeo.sampleHeaders(null, "V4"));

        shutdownMockWebServer();
    }

    @FixedHeader(name = "H1", value = "V1")
    @FixedHeader(name = "H2", value = "V2")
    @Mapping("${root}:41140")
    @OhClient
    public interface HeaderHttpClient {

        String sampleDefault();

        @FixedHeader(name = "H2")
        @FixedHeader(name = "H3", value = "V3")
        String sampleMapping();

        @FixedHeader(name = "H2")
        @FixedHeader(name = "H3", value = "V3")
        String sampleHeaders(@Header("H3") String v3,
                             @Header("H4") String v4);
    }

    @Mapping("${root}:41140")
    @OhClient
    @ConfigureWith(HeaderHttpClientConfig.class)
    public interface HeaderHttpClientNeo {

        String sampleDefault();

        @ConfigureWith(SampleMappingConfig.class)
        String sampleMapping();

        @ConfigureWith(SampleMappingConfig.class)
        String sampleHeaders(@Header("H3") String v3,
                             @Header("H4") String v4);
    }
}
