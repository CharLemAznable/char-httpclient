package com.github.charlemaznable.httpclient.wfclient.resilience4j;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.annotation.ResilienceRateLimiter;
import com.github.charlemaznable.httpclient.common.resilience4j.CommonRateLimiterTest;
import com.github.charlemaznable.httpclient.wfclient.WfClient;
import com.github.charlemaznable.httpclient.wfclient.WfFactory;
import io.github.resilience4j.ratelimiter.RateLimiter;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RateLimiterTest extends CommonRateLimiterTest {

    @SneakyThrows
    @Test
    public void testWfRateLimiter() {
        startMockWebServer();

        val wfLoader = WfFactory.wfLoader(reflectFactory());
        val httpClient = wfLoader.getClient(RateLimiterClient.class);

        val service = new Thread[4];
        for (int i = 0; i < 4; i++) {
            service[i] = new Thread(() -> checkOptionalException(() ->
                    assertEquals("OK", httpClient.getWithConfig().block())), "service" + i);
            service[i].start();
        }
        for (int i = 0; i < 4; i++) {
            service[i].join();
        }
        assertEquals(2, countSample.get());

        val service2 = new Thread[4];
        for (int i = 0; i < 4; i++) {
            service2[i] = new Thread(() -> checkOptionalException(() ->
                    assertEquals("OK", httpClient.getWithParam(null).block())), "service2" + i);
            service2[i].start();
        }
        for (int i = 0; i < 4; i++) {
            service2[i].join();
        }
        assertEquals(6, countSample.get());

        val service3 = new Thread[4];
        for (int i = 0; i < 4; i++) {
            service3[i] = new Thread(() -> checkOptionalException(() ->
                    assertEquals("OK", httpClient.getWithAnno().get())), "service3" + i);
            service3[i].start();
        }
        for (int i = 0; i < 4; i++) {
            service3[i].join();
        }
        assertEquals(8, countSample.get());

        val service4 = new Thread[4];
        for (int i = 0; i < 4; i++) {
            service4[i] = new Thread(() -> checkOptionalException(() ->
                    assertEquals("OK", httpClient.getWithDisableConfig().toCompletableFuture().get())), "service4" + i);
            service4[i].start();
        }
        for (int i = 0; i < 4; i++) {
            service4[i].join();
        }
        assertEquals(12, countSample.get());

        shutdownMockWebServer();
    }

    @Mapping("${root}:41420/sample")
    @MappingMethodNameDisabled
    @WfClient
    @ResilienceRateLimiter
    public interface RateLimiterClient {

        @ConfigureWith(CustomRateLimiterConfig.class)
        Mono<String> getWithConfig();

        Mono<String> getWithParam(RateLimiter rateLimiter);

        @ResilienceRateLimiter(limitForPeriod = 2,
                limitRefreshPeriodInNanos = 2000_000_000L,
                timeoutDurationInMillis = 0)
        Future<String> getWithAnno();

        @ConfigureWith(DisabledRateLimiterConfig.class)
        CompletionStage<String> getWithDisableConfig();
    }
}
