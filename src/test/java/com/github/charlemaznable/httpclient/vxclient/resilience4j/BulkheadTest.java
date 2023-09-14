package com.github.charlemaznable.httpclient.vxclient.resilience4j;

import com.github.charlemaznable.core.lang.Listt;
import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.annotation.ResilienceBulkhead;
import com.github.charlemaznable.httpclient.common.resilience4j.CommonBulkheadTest;
import com.github.charlemaznable.httpclient.vxclient.VxClient;
import com.github.charlemaznable.httpclient.vxclient.VxFactory;
import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.vertx.core.CompositeFuture;
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

        val getWithConfigs = Listt.<Future<String>>newArrayList();
        for (int i = 0; i < 10; i++) {
            getWithConfigs.add(httpClient.getWithConfig()
                    .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
        }
        Future.all(getWithConfigs).onComplete(result -> {
            test.verify(() -> assertEquals(5, countSample.get()));

            val getWithParams = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 10; i++) {
                getWithParams.add(httpClient.getWithParam(null)
                        .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
            }
            Future.all(getWithParams).onComplete(result2 -> {
                test.verify(() -> assertEquals(15, countSample.get()));

                val getWithAnno = Listt.<Future<String>>newArrayList();
                for (int i = 0; i < 10; i++) {
                    getWithAnno.add(httpClient.getWithAnno()
                            .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
                }
                Future.all(getWithAnno).onComplete(result3 -> {
                    test.verify(() -> assertEquals(20, countSample.get()));

                    val getWithDisableConfigs = Listt.<Future<String>>newArrayList();
                    for (int i = 0; i < 10; i++) {
                        getWithDisableConfigs.add(httpClient.getWithDisableConfig()
                                .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
                    }
                    Future.all(getWithDisableConfigs).onComplete(result4 -> {
                        test.verify(() -> assertEquals(30, countSample.get()));

                        shutdownMockWebServer();
                        test.<CompositeFuture>succeedingThenComplete().handle(result4);
                    });
                });
            });
        });
    }

    @Mapping("${root}:41410/sample")
    @MappingMethodNameDisabled
    @VxClient
    @ConfigureWith(DefaultBulkheadConfig.class)
    public interface BulkheadClient {

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
