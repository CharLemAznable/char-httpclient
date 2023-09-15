package com.github.charlemaznable.httpclient.ohclient.resilience4j;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceBulkhead;
import com.github.charlemaznable.httpclient.common.resilience4j.CommonBulkheadTest;
import com.github.charlemaznable.httpclient.ohclient.OhClient;
import com.github.charlemaznable.httpclient.ohclient.OhFactory;
import io.github.resilience4j.bulkhead.Bulkhead;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.jooq.lambda.Sneaky.runnable;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BulkheadTest extends CommonBulkheadTest {

    @SneakyThrows
    @Test
    public void testOhBulkhead() {
        startMockWebServer();

        val ohLoader = OhFactory.ohLoader(reflectFactory());
        val httpClient = ohLoader.getClient(BulkheadClient.class);

        val service = new Thread[10];
        for (int i = 0; i < 10; i++) {
            service[i] = new Thread(runnable(() ->
                    assertEquals("OK", httpClient.getWithConfig())), "service" + i);
            service[i].start();
        }
        for (int i = 0; i < 10; i++) {
            service[i].join();
        }
        assertEquals(5, countSample.get());

        val service2 = new Thread[10];
        for (int i = 0; i < 10; i++) {
            service2[i] = new Thread(runnable(() ->
                    assertEquals("OK", httpClient.getWithParam(null))), "service2" + i);
            service2[i].start();
        }
        for (int i = 0; i < 10; i++) {
            service2[i].join();
        }
        assertEquals(15, countSample.get());

        val service3 = new Thread[10];
        for (int i = 0; i < 10; i++) {
            service3[i] = new Thread(runnable(() ->
                    assertEquals("OK", httpClient.getWithAnno().get())), "service3" + i);
            service3[i].start();
        }
        for (int i = 0; i < 10; i++) {
            service3[i].join();
        }
        assertEquals(20, countSample.get());

        val service4 = new Thread[10];
        for (int i = 0; i < 10; i++) {
            service4[i] = new Thread(runnable(() ->
                    assertEquals("OK", httpClient.getWithDisableConfig().toCompletableFuture().get())), "service4" + i);
            service4[i].start();
        }
        for (int i = 0; i < 10; i++) {
            service4[i].join();
        }
        assertEquals(30, countSample.get());

        shutdownMockWebServer();
    }

    @Mapping("${root}:41410/sample")
    @MappingMethodNameDisabled
    @OhClient
    @ConfigureWith(DefaultBulkheadConfig.class)
    public interface BulkheadClient {

        @ConfigureWith(CustomBulkheadConfig.class)
        String getWithConfig();

        String getWithParam(Bulkhead bulkhead);

        @ResilienceBulkhead(maxConcurrentCalls = 5,
                fallback = CustomResilienceBulkheadRecover.class)
        Future<String> getWithAnno();

        @ConfigureWith(DisabledBulkheadConfig.class)
        CompletionStage<String> getWithDisableConfig();
    }
}
