package com.github.charlemaznable.httpclient.common;

import com.github.charlemaznable.httpclient.configurer.FixedHeadersConfigurer;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;

import java.util.List;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class CommonHeaderTest {

    protected MockWebServer mockWebServer;

    @SneakyThrows
    protected void startMockWebServer() {
        mockWebServer = new MockWebServer();
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
    }

    @SneakyThrows
    protected void shutdownMockWebServer() {
        mockWebServer.shutdown();
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
