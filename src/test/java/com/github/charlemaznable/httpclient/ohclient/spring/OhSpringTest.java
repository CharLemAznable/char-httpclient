package com.github.charlemaznable.httpclient.ohclient.spring;

import com.github.charlemaznable.core.spring.SpringContext;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.ohclient.testclient.TestComponentSpring;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringJUnitConfig(OhSpringConfiguration.class)
public class OhSpringTest {

    private static final String SAMPLE = "Sample";
    @Autowired
    private TestComponentSpring testComponent;

    @SneakyThrows
    @Test
    public void testOhClient() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    switch (requireNonNull(request.getPath())) {
                        case "/sample":
                            return new MockResponse().setBody(SAMPLE);
                        case "/SpringSpring-SpringSpring-GuiceGuice":
                            return new MockResponse().setBody("Done");
                        default:
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            });
            mockWebServer.start(41102);

            val testHttpClient = testComponent.getTestHttpClient();
            assertEquals(SAMPLE, testHttpClient.sample());
            assertEquals("{Sample}", testHttpClient.sampleWrapper());
            assertEquals(SAMPLE, testHttpClient.sampleWrap());
            assertEquals("Done", testHttpClient.sampleByContext());
            assertEquals(SAMPLE, testHttpClient.sample());
            assertEquals("{Sample}", testHttpClient.sampleWrapper());
            assertEquals(SAMPLE, testHttpClient.sampleWrap());
            assertEquals("Done", testHttpClient.sampleByContext());

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
