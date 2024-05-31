package com.github.charlemaznable.httpclient.ohclient.guice;

import com.github.charlemaznable.configservice.diamond.DiamondModular;
import com.github.charlemaznable.httpclient.common.guice.CommonGuiceTest;
import com.github.charlemaznable.httpclient.common.testclient.TestClientScanAnchor;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientConcrete;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientMocked;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientNone;
import com.github.charlemaznable.httpclient.ohclient.OhException;
import com.github.charlemaznable.httpclient.ohclient.OhModular;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OhGuiceTest extends CommonGuiceTest {

    @BeforeAll
    public static void beforeAll() {
        MockDiamondServer.setUpMockServer();
    }

    @AfterAll
    public static void afterAll() {
        MockDiamondServer.tearDownMockServer();
    }

    @Test
    public void testOhClient() {
        val diamondModular = new DiamondModular();
        val diamondModule = diamondModular.createModule();
        val ohModular = new OhModular(diamondModule).bindClasses(
                TestHttpClientMocked.class,
                TestHttpClientConcrete.class, TestHttpClientNone.class);
        val injector = Guice.createInjector(ohModular.createModule());

        startMockWebServer();

        val testHttpClient2 = injector.getInstance(TestHttpClientMocked.class);
        assertEquals(SAMPLE_RESULT, testHttpClient2.ohSample());
        assertEquals(SAMPLE_RESULT_WRAP_I, testHttpClient2.ohSampleWrapper());

        assertNull(injector.getInstance(TestHttpClientConcrete.class));

        assertNull(injector.getInstance(TestHttpClientNone.class));

        shutdownMockWebServer();
    }

    @Test
    public void testOhClientError() {
        val ohModular = new OhModular(emptyList()).bindClasses(
                TestHttpClientMocked.class,
                TestHttpClientConcrete.class, TestHttpClientNone.class);
        val injector = Guice.createInjector(ohModular.createModule());

        startMockWebServerError();

        val testHttpClient2 = injector.getInstance(TestHttpClientMocked.class);
        assertEquals(SAMPLE_ERROR_RESULT, testHttpClient2.ohSample());
        assertEquals(SAMPLE_ERROR_RESULT_WRAP_I, testHttpClient2.ohSampleWrapper());

        assertNull(injector.getInstance(TestHttpClientConcrete.class));

        assertNull(injector.getInstance(TestHttpClientNone.class));

        shutdownMockWebServerError();
    }

    @Test
    public void testOhClientNaked() {
        val ohModular = new OhModular();

        startMockWebServerNaked();

        val emptyInjector = Guice.createInjector(ohModular.createModule() /* required for provision */);
        assertThrows(ConfigurationException.class, () ->
                emptyInjector.getInstance(TestHttpClientMocked.class));

        val testHttpClient2 = ohModular.getClient(TestHttpClientMocked.class);
        assertEquals(SAMPLE_ERROR_RESULT, testHttpClient2.ohSample());
        assertEquals(SAMPLE_ERROR_RESULT_WRAP_I, testHttpClient2.ohSampleWrapper());

        assertThrows(OhException.class,
                () -> ohModular.getClient(TestHttpClientConcrete.class));

        assertThrows(OhException.class,
                () -> ohModular.getClient(TestHttpClientNone.class));

        shutdownMockWebServerNaked();
    }

    @Test
    public void testOhClientScan() {
        val diamondModular = new DiamondModular();
        val diamondModule = diamondModular.createModule();
        val ohModular = new OhModular(diamondModule).scanPackageClasses(TestClientScanAnchor.class);
        val injector = Guice.createInjector(ohModular.createModule());

        startMockWebServerScan();

        val testHttpClient2 = injector.getInstance(TestHttpClientMocked.class);
        assertEquals(SAMPLE_RESULT, testHttpClient2.ohSample());
        assertEquals(SAMPLE_RESULT_WRAP_I, testHttpClient2.ohSampleWrapper());

        assertNull(injector.getInstance(TestHttpClientConcrete.class));

        assertThrows(ConfigurationException.class, () ->
                injector.getInstance(TestHttpClientNone.class));

        shutdownMockWebServerScan();
    }
}
