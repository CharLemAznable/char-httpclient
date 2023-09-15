package com.github.charlemaznable.httpclient.vxclient.resilience4j;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceFallback;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceRetry;
import com.github.charlemaznable.httpclient.common.StatusError;
import com.github.charlemaznable.httpclient.common.resilience4j.CommonRetryTest;
import com.github.charlemaznable.httpclient.resilience.common.ResilienceMeterBinder;
import com.github.charlemaznable.httpclient.vxclient.VxClient;
import com.github.charlemaznable.httpclient.vxclient.VxFactory;
import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class RetryTest extends CommonRetryTest {

    @Test
    public void testVxRetry(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));
        val httpClient = vxLoader.getClient(RetryClient.class);
        httpClient.bindTo(new SimpleMeterRegistry());

        countSample.set(0);
        httpClient.getWithConfig().onSuccess(response -> {
            test.verify(() -> assertEquals("OK", response));

            countSample.set(0);
            httpClient.getWithParam(null).onSuccess(response1 -> {
                test.verify(() -> assertEquals("NotOK", response1));

                httpClient.bindTo(null);

                countSample.set(0);
                httpClient.getWithAnno().onSuccess(response2 -> {
                    test.verify(() -> assertEquals("NotOK", response2));

                    countSample.set(0);
                    httpClient.getWithDisableConfig().onFailure(exception3 -> {
                        test.verify(() -> assertTrue(exception3 instanceof StatusError));

                        shutdownMockWebServer();
                        test.completeNow();
                    });
                });
            });
        });
    }

    @Mapping("${root}:41440/sample")
    @MappingMethodNameDisabled
    @VxClient
    @ConfigureWith(DefaultRetryConfig.class)
    public interface RetryClient extends ResilienceMeterBinder {

        @ConfigureWith(CustomRetryConfig.class)
        Future<String> getWithConfig();

        @ResilienceRetry
        @ConfigureWith(CustomFallbackConfig.class)
        Future<String> getWithParam(Retry retry);

        @ResilienceRetry(maxAttempts = 2, isolatedExecutor = true)
        @ResilienceFallback(CustomResilienceRecover.class)
        Future<String> getWithAnno();

        @ConfigureWith(DisabledRetryConfig.class)
        Future<String> getWithDisableConfig();
    }
}
