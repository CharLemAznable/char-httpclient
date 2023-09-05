package com.github.charlemaznable.httpclient.vxclient.guice;

import com.github.charlemaznable.configservice.diamond.DiamondModular;
import com.github.charlemaznable.httpclient.common.guice.CommonGuiceTest;
import com.github.charlemaznable.httpclient.common.testclient.TestClientScanAnchor;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientConcrete;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientMocked;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientNone;
import com.github.charlemaznable.httpclient.vxclient.VxException;
import com.github.charlemaznable.httpclient.vxclient.VxModular;
import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.util.Providers;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.n3r.diamond.client.impl.MockDiamondServer;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(VertxExtension.class)
public class VxGuiceTest extends CommonGuiceTest {

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
    public void testVxClient(Vertx vertx, VertxTestContext test) {
        val diamondModular = new DiamondModular();
        val diamondModule = diamondModular.createModule();
        val vxModular = new VxModular(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Vertx.class).toProvider(Providers.of(vertx));
            }
        }, diamondModule).bindClasses(
                TestHttpClientMocked.class,
                TestHttpClientConcrete.class, TestHttpClientNone.class);
        val injector = Guice.createInjector(vxModular.createModule());

        startMockWebServer();

        assertNull(injector.getInstance(TestHttpClientConcrete.class));

        assertNull(injector.getInstance(TestHttpClientNone.class));

        val testHttpClient2 = injector.getInstance(TestHttpClientMocked.class);
        Future.all(newArrayList(
                testHttpClient2.vxSample().onSuccess(response -> test.verify(() -> assertEquals(SAMPLE_RESULT, response))),
                testHttpClient2.vxSampleWrapper().onSuccess(response -> test.verify(() -> assertEquals(SAMPLE_RESULT_WRAP_I, response)))
        )).onComplete(result -> {
            shutdownMockWebServer();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    @SneakyThrows
    @Test
    public void testVxClientError(Vertx vertx, VertxTestContext test) {
        val vxModular = new VxModular(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Vertx.class).toProvider(Providers.of(vertx));
            }
        }).bindClasses(
                TestHttpClientMocked.class,
                TestHttpClientConcrete.class, TestHttpClientNone.class);
        val injector = Guice.createInjector(vxModular.createModule());

        startMockWebServerError();

        assertNull(injector.getInstance(TestHttpClientConcrete.class));

        assertNull(injector.getInstance(TestHttpClientNone.class));

        val testHttpClient2 = injector.getInstance(TestHttpClientMocked.class);
        Future.all(newArrayList(
                testHttpClient2.vxSample().onSuccess(response -> test.verify(() -> assertEquals(SAMPLE_ERROR_RESULT, response))),
                testHttpClient2.vxSampleWrapper().onSuccess(response -> test.verify(() -> assertEquals(SAMPLE_ERROR_RESULT_WRAP_I, response)))
        )).onComplete(result -> {
            shutdownMockWebServerError();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    @SneakyThrows
    @Test
    public void testVxClientNaked(Vertx vertx, VertxTestContext test) {
        val vxModular = new VxModular(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Vertx.class).toProvider(Providers.of(vertx));
            }
        });

        startMockWebServerNaked();

        val emptyInjector = Guice.createInjector(vxModular.createModule() /* required for provision */);

        assertThrows(VxException.class,
                () -> vxModular.getClient(TestHttpClientConcrete.class));

        assertThrows(VxException.class,
                () -> vxModular.getClient(TestHttpClientNone.class));

        val testHttpClient2 = vxModular.getClient(TestHttpClientMocked.class);
        Future.all(newArrayList(
                testHttpClient2.vxSample().onSuccess(response -> test.verify(() -> assertEquals(SAMPLE_ERROR_RESULT, response))),
                testHttpClient2.vxSampleWrapper().onSuccess(response -> test.verify(() -> assertEquals(SAMPLE_ERROR_RESULT_WRAP_I, response)))
        )).onComplete(result -> {
            shutdownMockWebServerNaked();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    @SneakyThrows
    @Test
    public void testVxClientScan(Vertx vertx, VertxTestContext test) {
        val diamondModular = new DiamondModular();
        val diamondModule = diamondModular.createModule();
        val vxModular = new VxModular(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Vertx.class).toProvider(Providers.of(vertx));
            }
        }, diamondModule).scanPackageClasses(TestClientScanAnchor.class);
        val injector = Guice.createInjector(vxModular.createModule());

        startMockWebServerScan();

        assertNull(injector.getInstance(TestHttpClientConcrete.class));

        assertThrows(ConfigurationException.class, () ->
                injector.getInstance(TestHttpClientNone.class));

        val testHttpClient2 = injector.getInstance(TestHttpClientMocked.class);
        Future.all(newArrayList(
                testHttpClient2.vxSample().onSuccess(response -> test.verify(() -> assertEquals(SAMPLE_RESULT, response))),
                testHttpClient2.vxSampleWrapper().onSuccess(response -> test.verify(() -> assertEquals(SAMPLE_RESULT_WRAP_I, response)))
        )).onComplete(result -> {
            shutdownMockWebServerScan();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }
}
