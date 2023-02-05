package com.github.charlemaznable.httpclient.ohclient.guice;

import com.github.charlemaznable.configservice.diamond.DiamondModular;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.ohclient.OhException;
import com.github.charlemaznable.httpclient.ohclient.OhModular;
import com.github.charlemaznable.httpclient.ohclient.testclient.TestClientScanAnchor;
import com.github.charlemaznable.httpclient.ohclient.testclient.TestHttpClientConcrete;
import com.github.charlemaznable.httpclient.ohclient.testclient.TestHttpClientIsolated;
import com.github.charlemaznable.httpclient.ohclient.testclient.TestHttpClientNone;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;

import javax.annotation.Nonnull;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OhGuiceTest {

    private static final String SAMPLE = "/sample";
    private static final String SAMPLE_RESULT = "Guice";
    private static final String SAMPLE_RESULT_WRAP = "{Guice}";
    private static final String SAMPLE_RESULT_WRAP_I = "[Guice]";
    private static final String CONTEXT = "/GuiceGuice-SpringSpring-GuiceGuice";
    private static final String CONTEXT_RESULT = "Done";
    private static final String SAMPLE_ERROR = "/sampleError";
    private static final String SAMPLE_ERROR_RESULT = "GuiceError";
    private static final String SAMPLE_NO_ERROR_RESULT = "GuiceNoError";
    private static final String SAMPLE_ERROR_RESULT_WRAP_I = "[GuiceError]";

    @BeforeAll
    public static void beforeAll() {
        MockDiamondServer.setUpMockServer();
    }

    @AfterAll
    public static void afterAll() {
        MockDiamondServer.tearDownMockServer();
    }

    @SneakyThrows
    @Test
    public void testOhClient() {
        val diamondModular = new DiamondModular();
        val diamondModule = diamondModular.createModule();
        val ohModular = new OhModular(diamondModule).bindClasses(
                TestHttpClientIsolated.class,
                TestHttpClientConcrete.class, TestHttpClientNone.class);
        val injector = Guice.createInjector(ohModular.createModule());

        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    return switch (requireNonNull(request.getPath())) {
                        case SAMPLE -> new MockResponse().setBody(SAMPLE_RESULT);
                        case CONTEXT -> new MockResponse().setBody(CONTEXT_RESULT);
                        default -> new MockResponse()
                                .setResponseCode(HttpStatus.NOT_FOUND.value())
                                .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    };
                }
            });
            mockWebServer.start(41102);

            val testHttpClient2 = injector.getInstance(TestHttpClientIsolated.class);
            assertEquals(SAMPLE_RESULT, testHttpClient2.sample());
            assertEquals(SAMPLE_RESULT_WRAP_I, testHttpClient2.sampleWrapper());

            assertNull(injector.getInstance(TestHttpClientConcrete.class));

            assertNull(injector.getInstance(TestHttpClientNone.class));
        }
    }

    @SneakyThrows
    @Test
    public void testOhClientError() {
        val ohModular = new OhModular(emptyList()).bindClasses(
                TestHttpClientIsolated.class,
                TestHttpClientConcrete.class, TestHttpClientNone.class);
        val injector = Guice.createInjector(ohModular.createModule());

        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    return switch (requireNonNull(request.getPath())) {
                        case SAMPLE -> new MockResponse().setBody(SAMPLE_ERROR_RESULT);
                        case SAMPLE_ERROR -> new MockResponse().setBody(SAMPLE_RESULT);
                        default -> new MockResponse()
                                .setResponseCode(HttpStatus.NOT_FOUND.value())
                                .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    };
                }
            });
            mockWebServer.start(41102);

            val testHttpClient2 = injector.getInstance(TestHttpClientIsolated.class);
            assertEquals(SAMPLE_ERROR_RESULT, testHttpClient2.sample());
            assertEquals(SAMPLE_ERROR_RESULT_WRAP_I, testHttpClient2.sampleWrapper());

            assertNull(injector.getInstance(TestHttpClientConcrete.class));

            assertNull(injector.getInstance(TestHttpClientNone.class));
        }
    }

    @SneakyThrows
    @Test
    public void testOhClientNaked() {
        val ohModular = new OhModular();

        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    return switch (requireNonNull(request.getPath())) {
                        case SAMPLE -> new MockResponse().setBody(SAMPLE_ERROR_RESULT);
                        case SAMPLE_ERROR -> new MockResponse().setBody(SAMPLE_NO_ERROR_RESULT);
                        default -> new MockResponse()
                                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .setBody(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
                    };
                }
            });
            mockWebServer.start(41102);

            val emptyInjector = Guice.createInjector(ohModular.createModule() /* required for provision */);

            val testHttpClient2 = ohModular.getClient(TestHttpClientIsolated.class);
            assertEquals(SAMPLE_ERROR_RESULT, testHttpClient2.sample());
            assertEquals(SAMPLE_ERROR_RESULT_WRAP_I, testHttpClient2.sampleWrapper());

            assertThrows(OhException.class,
                    () -> ohModular.getClient(TestHttpClientConcrete.class));

            assertThrows(OhException.class,
                    () -> ohModular.getClient(TestHttpClientNone.class));
        }
    }

    @SneakyThrows
    @Test
    public void testOhClientScan() {
        val diamondModular = new DiamondModular();
        val diamondModule = diamondModular.createModule();
        val ohModular = new OhModular(diamondModule).scanPackageClasses(TestClientScanAnchor.class);
        val injector = Guice.createInjector(ohModular.createModule());

        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    return new MockResponse().setBody(SAMPLE_RESULT);
                }
            });
            mockWebServer.start(41102);

            val testHttpClient2 = injector.getInstance(TestHttpClientIsolated.class);
            assertEquals(SAMPLE_RESULT, testHttpClient2.sample());
            assertEquals(SAMPLE_RESULT_WRAP_I, testHttpClient2.sampleWrapper());

            assertNull(injector.getInstance(TestHttpClientConcrete.class));

            assertThrows(ConfigurationException.class, () ->
                    injector.getInstance(TestHttpClientNone.class));
        }
    }
}
