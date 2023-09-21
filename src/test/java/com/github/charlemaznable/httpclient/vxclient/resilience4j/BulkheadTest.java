package com.github.charlemaznable.httpclient.vxclient.resilience4j;

import com.github.charlemaznable.core.lang.Listt;
import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.common.resilience4j.CommonBulkheadTest;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceBulkhead;
import com.github.charlemaznable.httpclient.resilience.common.ResilienceMeterBinder;
import com.github.charlemaznable.httpclient.vxclient.VxClient;
import com.github.charlemaznable.httpclient.vxclient.VxFactory;
import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class BulkheadTest extends CommonBulkheadTest {

    @Timeout(value = 1, timeUnit = TimeUnit.MINUTES)
    @Test
    public void testVxBulkhead(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));
        val httpClient = vxLoader.getClient(BulkheadClient.class);
        httpClient.bindTo(new SimpleMeterRegistry());

        val getWithConfigs = Listt.<Future<String>>newArrayList();
        for (int i = 0; i < 10; i++) {
            getWithConfigs.add(httpClient.getWithConfig()
                    .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
        }
        Future.all(getWithConfigs).compose(result -> {
            test.verify(() -> assertEquals(5, countSample.get()));

            val getWithParams = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 10; i++) {
                getWithParams.add(httpClient.getWithParam(null)
                        .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
            }
            return Future.all(getWithParams);
        }).compose(result -> {
            test.verify(() -> assertEquals(15, countSample.get()));

            httpClient.bindTo(null);

            val getWithAnno = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 10; i++) {
                getWithAnno.add(httpClient.getWithAnno()
                        .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
            }
            return Future.all(getWithAnno);
        }).compose(result -> {
            test.verify(() -> assertEquals(20, countSample.get()));

            val getWithDisableConfigs = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 10; i++) {
                getWithDisableConfigs.add(httpClient.getWithDisableConfig()
                        .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
            }
            return Future.all(getWithDisableConfigs);
        }).compose(result -> {
            test.verify(() -> assertEquals(30, countSample.get()));

            shutdownMockWebServer();
            return Future.succeededFuture();
        }).onComplete(test.succeedingThenComplete());
    }

    @Mapping("${root}:41410/sample")
    @MappingMethodNameDisabled
    @VxClient
    @ConfigureWith(DefaultBulkheadConfig.class)
    public interface BulkheadClient extends ResilienceMeterBinder {

        @ConfigureWith(CustomBulkheadConfig.class)
        Future<String> getWithConfig();

        Future<String> getWithParam(Bulkhead bulkhead);

        @ResilienceBulkhead(maxConcurrentCalls = 5,
                fallback = CustomResilienceBulkheadRecover.class)
        Future<String> getWithAnno();

        @ConfigureWith(DisabledBulkheadConfig.class)
        Future<String> getWithDisableConfig();
    }
}
