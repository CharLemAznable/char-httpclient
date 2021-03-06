package com.github.charlemaznable.httpclient.ohclient.guice;

import com.github.charlemaznable.configservice.diamond.DiamondModular;
import com.github.charlemaznable.core.guice.GuiceFactory;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.ohclient.OhException;
import com.github.charlemaznable.httpclient.ohclient.OhModular;
import com.github.charlemaznable.httpclient.ohclient.testclient.TestClientScanAnchor;
import com.github.charlemaznable.httpclient.ohclient.testclient.TestComponentGuice;
import com.github.charlemaznable.httpclient.ohclient.testclient.TestHttpClient;
import com.github.charlemaznable.httpclient.ohclient.testclient.TestHttpClientConcrete;
import com.github.charlemaznable.httpclient.ohclient.testclient.TestHttpClientIsolated;
import com.github.charlemaznable.httpclient.ohclient.testclient.TestHttpClientNone;
import com.github.charlemaznable.httpclient.ohclient.testclient.TestSampleUrlProvider;
import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.util.Providers;
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

import static java.util.Collections.emptyList;
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
        val diamondModular = new DiamondModular().bindClasses(TestSampleUrlProvider.class);
        val diamondModule = diamondModular.createModule();
        val ohModular = new OhModular(diamondModule).bindClasses(
                TestHttpClient.class, TestHttpClientIsolated.class,
                TestHttpClientConcrete.class, TestHttpClientNone.class);
        val injector = Guice.createInjector(ohModular.createModule());

        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    switch (request.getPath()) {
                        case SAMPLE:
                            return new MockResponse().setBody(SAMPLE_RESULT);
                        case CONTEXT:
                            return new MockResponse().setBody(CONTEXT_RESULT);
                        default:
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            });
            mockWebServer.start(41102);

            val testComponent = injector.getInstance(TestComponentGuice.class);
            val testHttpClient = testComponent.getTestHttpClient();
            assertEquals(SAMPLE_RESULT, testHttpClient.sample());
            assertEquals(SAMPLE_RESULT_WRAP, testHttpClient.sampleWrapper());
            assertEquals(SAMPLE_RESULT, testHttpClient.sampleWrap());
            assertEquals(CONTEXT_RESULT, testHttpClient.sampleByContext());
            assertEquals(SAMPLE_RESULT, testHttpClient.sample());
            assertEquals(SAMPLE_RESULT_WRAP, testHttpClient.sampleWrapper());
            assertEquals(SAMPLE_RESULT, testHttpClient.sampleWrap());
            assertEquals(CONTEXT_RESULT, testHttpClient.sampleByContext());

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
                TestHttpClient.class, TestHttpClientIsolated.class,
                TestHttpClientConcrete.class, TestHttpClientNone.class);
        val injector = Guice.createInjector(ohModular.createModule());

        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    switch (request.getPath()) {
                        case SAMPLE:
                            return new MockResponse().setBody(SAMPLE_ERROR_RESULT);
                        case SAMPLE_ERROR:
                            return new MockResponse().setBody(SAMPLE_RESULT);
                        default:
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            });
            mockWebServer.start(41102);

            val testComponent = injector.getInstance(TestComponentGuice.class);
            val testHttpClient = testComponent.getTestHttpClient();
            assertThrows(NullPointerException.class, testHttpClient::sample);
            assertThrows(NullPointerException.class, testHttpClient::sampleWrapper);
            assertEquals(SAMPLE_RESULT, testHttpClient.sampleWrap());

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
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    switch (request.getPath()) {
                        case SAMPLE:
                            return new MockResponse().setBody(SAMPLE_ERROR_RESULT);
                        case SAMPLE_ERROR:
                            return new MockResponse().setBody(SAMPLE_NO_ERROR_RESULT);
                        default:
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                    .setBody(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
                    }
                }
            });
            mockWebServer.start(41102);

            val emptyInjector = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(TestHttpClient.class).toProvider(Providers.of(null));
                }
            }, ohModular.createModule() /* required for provision */);

            val testHttpClient = emptyInjector.getInstance(TestComponentGuice.class).getTestHttpClient();
            assertThrows(NullPointerException.class, testHttpClient::sample);
            assertThrows(NullPointerException.class, testHttpClient::sampleWrapper);
            assertEquals(SAMPLE_NO_ERROR_RESULT, testHttpClient.sampleWrap());

            val testHttpClient2 = ohModular.getClient(TestHttpClientIsolated.class);
            assertEquals(SAMPLE_ERROR_RESULT, testHttpClient2.sample());
            assertEquals(SAMPLE_ERROR_RESULT_WRAP_I, testHttpClient2.sampleWrapper());

            assertThrows(OhException.class,
                    () -> ohModular.getClient(TestHttpClientConcrete.class));

            assertThrows(OhException.class,
                    () -> ohModular.getClient(TestHttpClientNone.class));

            val injector = Guice.createInjector(ohModular.createModule());
            assertThrows(ConfigurationException.class, () ->
                    injector.getInstance(TestHttpClient.class));
            assertNull(new GuiceFactory(injector).build(TestHttpClient.class));
        }
    }

    @SneakyThrows
    @Test
    public void testOhClientScan() {
        val diamondModular = new DiamondModular().bindClasses(TestSampleUrlProvider.class);
        val diamondModule = diamondModular.createModule();
        val ohModular = new OhModular(diamondModule).scanPackageClasses(TestClientScanAnchor.class);
        val injector = Guice.createInjector(ohModular.createModule());

        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    return new MockResponse().setBody(SAMPLE_RESULT);
                }
            });
            mockWebServer.start(41102);

            val testComponent = injector.getInstance(TestComponentGuice.class);
            val testHttpClient = testComponent.getTestHttpClient();
            assertEquals(SAMPLE_RESULT, testHttpClient.sample());
            assertEquals(SAMPLE_RESULT_WRAP, testHttpClient.sampleWrapper());
            assertEquals(SAMPLE_RESULT, testHttpClient.sampleWrap());
            assertEquals(SAMPLE_RESULT, testHttpClient.sampleByContext());
            assertEquals(SAMPLE_RESULT, testHttpClient.sample());
            assertEquals(SAMPLE_RESULT_WRAP, testHttpClient.sampleWrapper());
            assertEquals(SAMPLE_RESULT, testHttpClient.sampleWrap());
            assertEquals(SAMPLE_RESULT, testHttpClient.sampleByContext());

            val testHttpClient2 = injector.getInstance(TestHttpClientIsolated.class);
            assertEquals(SAMPLE_RESULT, testHttpClient2.sample());
            assertEquals(SAMPLE_RESULT_WRAP_I, testHttpClient2.sampleWrapper());

            assertNull(injector.getInstance(TestHttpClientConcrete.class));

            assertThrows(ConfigurationException.class, () ->
                    injector.getInstance(TestHttpClientNone.class));
        }
    }
}
