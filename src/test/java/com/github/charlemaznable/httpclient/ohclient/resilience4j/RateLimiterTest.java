package com.github.charlemaznable.httpclient.ohclient.resilience4j;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.common.resilience4j.CommonRateLimiterTest;
import com.github.charlemaznable.httpclient.ohclient.OhClient;
import com.github.charlemaznable.httpclient.ohclient.OhFactory;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceRateLimiter;
import com.github.charlemaznable.httpclient.resilience.common.ResilienceMeterBinder;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.jooq.lambda.Sneaky.runnable;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RateLimiterTest extends CommonRateLimiterTest {

    @SneakyThrows
    @Test
    public void testOhRateLimiter() {
        startMockWebServer();

        val ohLoader = OhFactory.ohLoader(reflectFactory());
        val httpClient = ohLoader.getClient(RateLimiterClient.class);
        httpClient.bindTo(new SimpleMeterRegistry());

        val service = new Thread[4];
        for (int i = 0; i < 4; i++) {
            service[i] = new Thread(runnable(() ->
                    assertEquals("OK", httpClient.getWithConfig())), "service" + i);
            service[i].start();
        }
        for (int i = 0; i < 4; i++) {
            service[i].join();
        }
        assertEquals(2, countSample.get());

        val service2 = new Thread[4];
        for (int i = 0; i < 4; i++) {
            service2[i] = new Thread(runnable(() ->
                    assertEquals("OK", httpClient.getWithParam(null))), "service2" + i);
            service2[i].start();
        }
        for (int i = 0; i < 4; i++) {
            service2[i].join();
        }
        assertEquals(6, countSample.get());

        httpClient.bindTo(null);

        val service3 = new Thread[4];
        for (int i = 0; i < 4; i++) {
            service3[i] = new Thread(runnable(() ->
                    assertEquals("OK", httpClient.getWithAnno().get())), "service3" + i);
            service3[i].start();
        }
        for (int i = 0; i < 4; i++) {
            service3[i].join();
        }
        assertEquals(8, countSample.get());

        val service4 = new Thread[4];
        for (int i = 0; i < 4; i++) {
            service4[i] = new Thread(runnable(() ->
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
    @OhClient
    @ConfigureWith(DefaultRateLimiterConfig.class)
    public interface RateLimiterClient extends ResilienceMeterBinder {

        @ConfigureWith(CustomRateLimiterConfig.class)
        String getWithConfig();

        String getWithParam(RateLimiter rateLimiter);

        @ResilienceRateLimiter(limitForPeriod = 2,
                limitRefreshPeriodInNanos = 2000_000_000L,
                timeoutDurationInMillis = 0,
                fallback = CustomResilienceRateLimiterRecover.class)
        Future<String> getWithAnno();

        @ConfigureWith(DisabledRateLimiterConfig.class)
        CompletionStage<String> getWithDisableConfig();
    }
}
