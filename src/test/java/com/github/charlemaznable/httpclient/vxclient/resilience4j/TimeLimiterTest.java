package com.github.charlemaznable.httpclient.vxclient.resilience4j;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.common.resilience4j.CommonTimeLimiterTest;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceTimeLimiter;
import com.github.charlemaznable.httpclient.resilience.common.ResilienceMeterBinder;
import com.github.charlemaznable.httpclient.vxclient.VxClient;
import com.github.charlemaznable.httpclient.vxclient.VxFactory;
import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class TimeLimiterTest extends CommonTimeLimiterTest {

    @Test
    public void testVxTimeLimiter(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));
        val httpClient = vxLoader.getClient(TimeLimiterClient.class);
        httpClient.bindTo(new SimpleMeterRegistry());

        httpClient.getWithConfig().onSuccess(response -> {
            test.verify(() -> assertEquals("Timeout", response));

            httpClient.getWithParam(null).onSuccess(response1 -> {
                test.verify(() -> assertEquals("OK", response1));

                httpClient.bindTo(null);

                httpClient.getWithAnno().onSuccess(response2 -> {
                    test.verify(() -> assertEquals("Timeout", response2));

                    httpClient.getWithDisableConfig().onSuccess(response3 -> {
                        test.verify(() -> assertEquals("OK", response3));

                        shutdownMockWebServer();
                        test.completeNow();
                    });
                });
            });
        });
    }

    @Mapping("${root}:41450/sample")
    @MappingMethodNameDisabled
    @VxClient
    @ConfigureWith(DefaultTimeLimiterConfig.class)
    public interface TimeLimiterClient extends ResilienceMeterBinder {

        @ConfigureWith(CustomTimeLimiterConfig.class)
        Future<String> getWithConfig();

        Future<String> getWithParam(TimeLimiter timeLimiter);

        @ResilienceTimeLimiter(
                fallback = CustomResilienceTimeLimiterRecover.class)
        Future<String> getWithAnno();

        @ConfigureWith(DisabledTimeLimiterConfig.class)
        Future<String> getWithDisableConfig();
    }
}
