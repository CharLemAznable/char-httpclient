package com.github.charlemaznable.httpclient.vxclient.resilience4j;

import com.github.charlemaznable.core.lang.Listt;
import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.annotation.ResilienceRateLimiter;
import com.github.charlemaznable.httpclient.common.resilience4j.CommonRateLimiterTest;
import com.github.charlemaznable.httpclient.vxclient.VxClient;
import com.github.charlemaznable.httpclient.vxclient.VxFactory;
import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class RateLimiterTest extends CommonRateLimiterTest {

    @Test
    public void testRateLimiter(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));
        val httpClient = vxLoader.getClient(RateLimiterClient.class);

        val getWithConfigs = Listt.<Future<String>>newArrayList();
        for (int i = 0; i < 4; i++) {
            getWithConfigs.add(checkOptionalException(httpClient.getWithConfig(), test));
        }
        Future.all(getWithConfigs).onComplete(result -> {
            test.verify(() -> assertEquals(2, countSample.get()));

            val getWithParams = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 4; i++) {
                getWithParams.add(checkOptionalException(httpClient.getWithParam(null), test));
            }
            Future.all(getWithParams).onComplete(result2 -> {
                test.verify(() -> assertEquals(6, countSample.get()));

                val getWithAnno = Listt.<Future<String>>newArrayList();
                for (int i = 0; i < 4; i++) {
                    getWithAnno.add(checkOptionalException(httpClient.getWithAnno(), test));
                }
                Future.all(getWithAnno).onComplete(result3 -> {
                    test.verify(() -> assertEquals(8, countSample.get()));

                    val getWithDisableConfigs = Listt.<Future<String>>newArrayList();
                    for (int i = 0; i < 4; i++) {
                        getWithDisableConfigs.add(checkOptionalException(httpClient.getWithDisableConfig(), test));
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

    private Future<String> checkOptionalException(Future<String> resultFuture, VertxTestContext test) {
        return resultFuture
                .onSuccess(response -> test.verify(() -> assertEquals("OK", response)))
                .otherwise(e -> {
                    if (e instanceof ExecutionException) {
                        test.verify(() -> assertTrue(e.getCause() instanceof RequestNotPermitted));
                    } else {
                        test.verify(() -> assertTrue(e instanceof RequestNotPermitted));
                    }
                    return null;
                });
    }

    @Mapping("${root}:41420/sample")
    @MappingMethodNameDisabled
    @VxClient
    @ResilienceRateLimiter
    public interface RateLimiterClient {

        @ConfigureWith(CustomRateLimiterConfig.class)
        Future<String> getWithConfig();

        Future<String> getWithParam(RateLimiter rateLimiter);

        @ResilienceRateLimiter(limitForPeriod = 2,
                limitRefreshPeriodInNanos = 2000_000_000L,
                timeoutDurationInMillis = 0)
        Future<String> getWithAnno();

        @ConfigureWith(DisabledRateLimiterConfig.class)
        Future<String> getWithDisableConfig();
    }
}
