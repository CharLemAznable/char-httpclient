package com.github.charlemaznable.httpclient.wfclient;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.FixedHeader;
import com.github.charlemaznable.httpclient.annotation.Header;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonHeaderTest;
import lombok.val;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HeaderTest extends CommonHeaderTest {

    @Test
    public void testHeader() {
        startMockWebServer();

        val wfLoader = WfFactory.wfLoader(reflectFactory());

        val httpClient = wfLoader.getClient(HeaderHttpClient.class);
        assertEquals("OK", httpClient.sampleDefault().block());
        assertEquals("OK", httpClient.sampleMapping().block());
        assertEquals("OK", httpClient.sampleHeaders(null, "V4").block());

        val httpClientNeo = wfLoader.getClient(HeaderHttpClientNeo.class);
        assertEquals("OK", httpClientNeo.sampleDefault().block());
        assertEquals("OK", httpClientNeo.sampleMapping().block());
        assertEquals("OK", httpClientNeo.sampleHeaders(null, "V4").block());

        shutdownMockWebServer();
    }

    @FixedHeader(name = "H1", value = "V1")
    @FixedHeader(name = "H2", value = "V2")
    @Mapping("${root}:41140")
    @WfClient
    public interface HeaderHttpClient {

        Mono<String> sampleDefault();

        @FixedHeader(name = "H2")
        @FixedHeader(name = "H3", value = "V3")
        Mono<String> sampleMapping();

        @FixedHeader(name = "H2")
        @FixedHeader(name = "H3", value = "V3")
        Mono<String> sampleHeaders(@Header("H3") String v3,
                                   @Header("H4") String v4);
    }

    @Mapping("${root}:41140")
    @WfClient
    @ConfigureWith(HeaderHttpClientConfig.class)
    public interface HeaderHttpClientNeo {

        Mono<String> sampleDefault();

        @ConfigureWith(SampleMappingConfig.class)
        Mono<String> sampleMapping();

        @ConfigureWith(SampleMappingConfig.class)
        Mono<String> sampleHeaders(@Header("H3") String v3,
                                   @Header("H4") String v4);
    }
}
