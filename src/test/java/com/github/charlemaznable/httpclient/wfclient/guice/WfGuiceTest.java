package com.github.charlemaznable.httpclient.wfclient.guice;

import com.github.charlemaznable.configservice.diamond.DiamondModular;
import com.github.charlemaznable.httpclient.common.guice.CommonGuiceTest;
import com.github.charlemaznable.httpclient.common.testclient.TestClientScanAnchor;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientConcrete;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientMocked;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientNone;
import com.github.charlemaznable.httpclient.wfclient.WfException;
import com.github.charlemaznable.httpclient.wfclient.WfModular;
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

public class WfGuiceTest extends CommonGuiceTest {

    @BeforeAll
    public static void beforeAll() {
        MockDiamondServer.setUpMockServer();
    }

    @AfterAll
    public static void afterAll() {
        MockDiamondServer.tearDownMockServer();
    }

    @Test
    public void testWfClient() {
        val diamondModular = new DiamondModular();
        val diamondModule = diamondModular.createModule();
        val wfModular = new WfModular(diamondModule).bindClasses(
                TestHttpClientMocked.class,
                TestHttpClientConcrete.class, TestHttpClientNone.class);
        val injector = Guice.createInjector(wfModular.createModule());

        startMockWebServer();

        val testHttpClient2 = injector.getInstance(TestHttpClientMocked.class);
        assertEquals(SAMPLE_RESULT, testHttpClient2.wfSample().block());
        assertEquals(SAMPLE_RESULT_WRAP_I, testHttpClient2.wfSampleWrapper().block());

        assertNull(injector.getInstance(TestHttpClientConcrete.class));

        assertNull(injector.getInstance(TestHttpClientNone.class));

        shutdownMockWebServer();
    }

    @Test
    public void testWfClientError() {
        val wfModular = new WfModular(emptyList()).bindClasses(
                TestHttpClientMocked.class,
                TestHttpClientConcrete.class, TestHttpClientNone.class);
        val injector = Guice.createInjector(wfModular.createModule());

        startMockWebServerError();

        val testHttpClient2 = injector.getInstance(TestHttpClientMocked.class);
        assertEquals(SAMPLE_ERROR_RESULT, testHttpClient2.wfSample().block());
        assertEquals(SAMPLE_ERROR_RESULT_WRAP_I, testHttpClient2.wfSampleWrapper().block());

        assertNull(injector.getInstance(TestHttpClientConcrete.class));

        assertNull(injector.getInstance(TestHttpClientNone.class));

        shutdownMockWebServerError();
    }

    @Test
    public void testWfClientNaked() {
        val wfModular = new WfModular();

        startMockWebServerNaked();

        val emptyInjector = Guice.createInjector(wfModular.createModule() /* required for provision */);

        val testHttpClient2 = wfModular.getClient(TestHttpClientMocked.class);
        assertEquals(SAMPLE_ERROR_RESULT, testHttpClient2.wfSample().block());
        assertEquals(SAMPLE_ERROR_RESULT_WRAP_I, testHttpClient2.wfSampleWrapper().block());

        assertThrows(WfException.class,
                () -> wfModular.getClient(TestHttpClientConcrete.class));

        assertThrows(WfException.class,
                () -> wfModular.getClient(TestHttpClientNone.class));

        shutdownMockWebServerNaked();
    }

    @Test
    public void testWfClientScan() {
        val diamondModular = new DiamondModular();
        val diamondModule = diamondModular.createModule();
        val wfModular = new WfModular(diamondModule).scanPackageClasses(TestClientScanAnchor.class);
        val injector = Guice.createInjector(wfModular.createModule());

        startMockWebServerScan();

        val testHttpClient2 = injector.getInstance(TestHttpClientMocked.class);
        assertEquals(SAMPLE_RESULT, testHttpClient2.wfSample().block());
        assertEquals(SAMPLE_RESULT_WRAP_I, testHttpClient2.wfSampleWrapper().block());

        assertNull(injector.getInstance(TestHttpClientConcrete.class));

        assertThrows(ConfigurationException.class, () ->
                injector.getInstance(TestHttpClientNone.class));

        shutdownMockWebServerScan();
    }
}
