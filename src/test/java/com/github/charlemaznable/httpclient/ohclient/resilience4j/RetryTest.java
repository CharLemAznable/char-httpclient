package com.github.charlemaznable.httpclient.ohclient.resilience4j;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.common.MeterBinder;
import com.github.charlemaznable.httpclient.common.StatusError;
import com.github.charlemaznable.httpclient.common.resilience4j.CommonRetryTest;
import com.github.charlemaznable.httpclient.ohclient.OhClient;
import com.github.charlemaznable.httpclient.ohclient.OhFactory;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceFallback;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceRetry;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RetryTest extends CommonRetryTest {

    @SneakyThrows
    @Test
    public void testOhRetry() {
        startMockWebServer();

        val ohLoader = OhFactory.ohLoader(reflectFactory());
        val httpClient = ohLoader.getClient(RetryClient.class);
        httpClient.bindTo(new SimpleMeterRegistry());

        countSample.set(0);
        assertEquals("OK", httpClient.getWithConfig());

        countSample.set(0);
        assertEquals("NotOK", httpClient.getWithParam(null));

        httpClient.bindTo(null);

        countSample.set(0);
        assertEquals("NotOK", httpClient.getWithAnno().get());

        countSample.set(0);
        try {
            httpClient.getWithDisableConfig().toCompletableFuture().get();
        } catch (Exception e) {
            assertTrue(e instanceof ExecutionException);
            assertTrue(e.getCause() instanceof StatusError);
        }

        val httpClient2 = ohLoader.getClient(RetryClient2.class);

        countSample.set(0);
        assertEquals("OK", httpClient2.getWithConfig());

        countSample.set(0);
        assertEquals("NotOK2", httpClient2.getWithAnno());

        shutdownMockWebServer();
    }

    @Mapping("${root}:41440/sample")
    @MappingMethodNameDisabled
    @OhClient
    @ConfigureWith(DefaultRetryConfig.class)
    public interface RetryClient extends MeterBinder {

        @ConfigureWith(CustomRetryConfig.class)
        String getWithConfig();

        @ResilienceRetry
        @ConfigureWith(CustomFallbackConfig.class)
        String getWithParam(Retry retry);

        @ResilienceRetry(maxAttempts = 2, isolatedExecutor = true)
        @ResilienceFallback(CustomResilienceRecover.class)
        Future<String> getWithAnno();

        @ConfigureWith(DisabledRetryConfig.class)
        CompletionStage<String> getWithDisableConfig();
    }

    @Mapping("${root}:41440/sample2")
    @MappingMethodNameDisabled
    @OhClient
    @ResilienceRetry
    public interface RetryClient2 {

        @ConfigureWith(CustomRetryConfig.class)
        String getWithConfig();

        @ResilienceRetry(maxAttempts = 2,
                retryOnResultPredicate = CustomResultPredicate.class,
                failAfterMaxAttempts = true,
                isolatedExecutor = true)
        @ResilienceFallback(CustomResilienceRecover2.class)
        String getWithAnno();
    }
}
