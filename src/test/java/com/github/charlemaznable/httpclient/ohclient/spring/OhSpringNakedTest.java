package com.github.charlemaznable.httpclient.ohclient.spring;

import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.ohclient.OhException;
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

import static com.github.charlemaznable.httpclient.ohclient.OhFactory.getClient;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringJUnitConfig(OhSpringNakedConfiguration.class)
public class OhSpringNakedTest {

    @SneakyThrows
    @Test
    public void testOhClientNaked() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    return switch (requireNonNull(request.getPath())) {
                        case "/sample" -> new MockResponse().setBody("SampleError");
                        case "/sampleError" -> new MockResponse().setBody("SampleNoError");
                        default -> new MockResponse()
                                .setResponseCode(HttpStatus.NOT_FOUND.value())
                                .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    };
                }
            });
            mockWebServer.start(41102);

            val testHttpClientIsolated = getClient(TestHttpClientIsolated.class);
            assertEquals("SampleError", testHttpClientIsolated.sample());
            assertEquals("[SampleError]", testHttpClientIsolated.sampleWrapper());

            assertThrows(OhException.class,
                    () -> getClient(TestHttpClientConcrete.class));

            assertThrows(OhException.class,
                    () -> getClient(TestHttpClientNone.class));
        }
    }
}
