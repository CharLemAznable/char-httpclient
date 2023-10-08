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
        httpClient.resilienceBindTo(new SimpleMeterRegistry());

        httpClient.getWithConfig().compose(response -> {
            test.verify(() -> assertEquals("Timeout", response));

            return httpClient.getWithParam(null);
        }).compose(response -> {
            test.verify(() -> assertEquals("OK", response));

            httpClient.resilienceBindTo(null);

            return httpClient.getWithAnno();
        }).compose(response -> {
            test.verify(() -> assertEquals("Timeout", response));

            return httpClient.getWithDisableConfig();
        }).compose(response -> {
            test.verify(() -> assertEquals("OK", response));

            shutdownMockWebServer();
            return Future.succeededFuture();
        }).onComplete(test.succeedingThenComplete());
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
