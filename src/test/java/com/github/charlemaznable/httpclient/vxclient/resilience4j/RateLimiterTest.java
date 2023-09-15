package com.github.charlemaznable.httpclient.vxclient.resilience4j;

import com.github.charlemaznable.core.lang.Listt;
import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.common.resilience4j.CommonRateLimiterTest;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceRateLimiter;
import com.github.charlemaznable.httpclient.resilience.common.ResilienceMeterBinder;
import com.github.charlemaznable.httpclient.vxclient.VxClient;
import com.github.charlemaznable.httpclient.vxclient.VxFactory;
import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class RateLimiterTest extends CommonRateLimiterTest {

    @Test
    public void testVxRateLimiter(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));
        val httpClient = vxLoader.getClient(RateLimiterClient.class);
        httpClient.bindTo(new SimpleMeterRegistry());

        val getWithConfigs = Listt.<Future<String>>newArrayList();
        for (int i = 0; i < 4; i++) {
            getWithConfigs.add(httpClient.getWithConfig()
                    .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
        }
        Future.all(getWithConfigs).onComplete(result -> {
            test.verify(() -> assertEquals(2, countSample.get()));

            val getWithParams = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 4; i++) {
                getWithParams.add(httpClient.getWithParam(null)
                        .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
            }
            Future.all(getWithParams).onComplete(result2 -> {
                test.verify(() -> assertEquals(6, countSample.get()));

                httpClient.bindTo(null);

                val getWithAnno = Listt.<Future<String>>newArrayList();
                for (int i = 0; i < 4; i++) {
                    getWithAnno.add(httpClient.getWithAnno()
                            .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
                }
                Future.all(getWithAnno).onComplete(result3 -> {
                    test.verify(() -> assertEquals(8, countSample.get()));

                    val getWithDisableConfigs = Listt.<Future<String>>newArrayList();
                    for (int i = 0; i < 4; i++) {
                        getWithDisableConfigs.add(httpClient.getWithDisableConfig()
                                .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
                    }
                    Future.all(getWithDisableConfigs).onComplete(result4 -> {
                        test.verify(() -> assertEquals(12, countSample.get()));

                        shutdownMockWebServer();
                        test.<CompositeFuture>succeedingThenComplete().handle(result4);
                    });
                });
            });
        });
    }

    @Mapping("${root}:41420/sample")
    @MappingMethodNameDisabled
    @VxClient
    @ConfigureWith(DefaultRateLimiterConfig.class)
    public interface RateLimiterClient extends ResilienceMeterBinder {

        @ConfigureWith(CustomRateLimiterConfig.class)
        Future<String> getWithConfig();

        Future<String> getWithParam(RateLimiter rateLimiter);

        @ResilienceRateLimiter(limitForPeriod = 2,
                limitRefreshPeriodInNanos = 2000_000_000L,
                timeoutDurationInMillis = 0,
                fallback = CustomResilienceRateLimiterRecover.class)
        Future<String> getWithAnno();

        @ConfigureWith(DisabledRateLimiterConfig.class)
        Future<String> getWithDisableConfig();
    }
}
