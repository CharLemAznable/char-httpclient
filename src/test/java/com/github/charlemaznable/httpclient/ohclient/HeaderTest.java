package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.common.ConfigureWith;
import com.github.charlemaznable.httpclient.common.FixedHeader;
import com.github.charlemaznable.httpclient.common.Header;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.configurer.FixedHeadersConfigurer;
import com.github.charlemaznable.httpclient.ohclient.OhFactory.OhLoader;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.util.List;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HeaderTest {

    private static final OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @SneakyThrows
    @Test
    public void testOhHeader() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    switch (requireNonNull(request.getPath())) {
                        case "/sampleDefault":
                            assertEquals("V1", request.getHeader("H1"));
                            assertEquals("V2", request.getHeader("H2"));
                            assertNull(request.getHeader("H3"));
                            assertNull(request.getHeader("H4"));
                            return new MockResponse().setBody("OK");
                        case "/sampleMapping":
                            assertEquals("V1", request.getHeader("H1"));
                            assertNull(request.getHeader("H2"));
                            assertEquals("V3", request.getHeader("H3"));
                            assertNull(request.getHeader("H4"));
                            return new MockResponse().setBody("OK");
                        case "/sampleHeaders":
                            assertEquals("V1", request.getHeader("H1"));
                            assertNull(request.getHeader("H2"));
                            assertNull(request.getHeader("H3"));
                            assertEquals("V4", request.getHeader("H4"));
                            return new MockResponse().setBody("OK");
                        default:
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            });
            mockWebServer.start(41140);

            val httpClient = ohLoader.getClient(HeaderHttpClient.class);
            assertEquals("OK", httpClient.sampleDefault());
            assertEquals("OK", httpClient.sampleMapping());
            assertEquals("OK", httpClient.sampleHeaders(null, "V4"));

            val httpClientNeo = ohLoader.getClient(HeaderHttpClientNeo.class);
            assertEquals("OK", httpClientNeo.sampleDefault());
            assertEquals("OK", httpClientNeo.sampleMapping());
            assertEquals("OK", httpClientNeo.sampleHeaders(null, "V4"));
        }
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

    public static class HeaderHttpClientConfig implements FixedHeadersConfigurer {

        @Override
        public List<Pair<String, String>> fixedHeaders() {
            return newArrayList(Pair.of("H1", "V1"), Pair.of("H2", "V2"));
        }
    }

    public static class SampleMappingConfig implements FixedHeadersConfigurer {

        @Override
        public List<Pair<String, String>> fixedHeaders() {
            return newArrayList(Pair.of("H2", null), Pair.of("H3", "V3"));
        }
    }
}
