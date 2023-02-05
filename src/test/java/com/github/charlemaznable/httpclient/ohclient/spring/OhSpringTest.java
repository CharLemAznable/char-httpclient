package com.github.charlemaznable.httpclient.ohclient.spring;

import com.github.charlemaznable.core.spring.SpringContext;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.ohclient.testclient.TestHttpClientConcrete;
import com.github.charlemaznable.httpclient.ohclient.testclient.TestHttpClientIsolated;
import com.github.charlemaznable.httpclient.ohclient.testclient.TestHttpClientNone;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringJUnitConfig(OhSpringConfiguration.class)
public class OhSpringTest {

    private static final String SAMPLE = "Sample";

    @SneakyThrows
    @Test
    public void testOhClient() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    return switch (requireNonNull(request.getPath())) {
                        case "/sample" -> new MockResponse().setBody(SAMPLE);
                        case "/SpringSpring-SpringSpring-GuiceGuice" -> new MockResponse().setBody("Done");
                        default -> new MockResponse()
                                .setResponseCode(HttpStatus.NOT_FOUND.value())
                                .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    };
                }
            });
            mockWebServer.start(41102);

            val testHttpClientIsolated = SpringContext.getBean(TestHttpClientIsolated.class);
            assertEquals(SAMPLE, testHttpClientIsolated.sample());
            assertEquals("[Sample]", testHttpClientIsolated.sampleWrapper());

            val testHttpClientConcrete = SpringContext.getBean(TestHttpClientConcrete.class);
            assertNull(testHttpClientConcrete);

            val testHttpClientNone = SpringContext.getBean(TestHttpClientNone.class);
            assertNull(testHttpClientNone);
        }
    }
}
