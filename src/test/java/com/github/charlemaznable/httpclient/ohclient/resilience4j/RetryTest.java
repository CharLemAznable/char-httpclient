package com.github.charlemaznable.httpclient.ohclient.resilience4j;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.annotation.ResilienceRetry;
import com.github.charlemaznable.httpclient.common.StatusError;
import com.github.charlemaznable.httpclient.common.resilience4j.CommonRetryTest;
import com.github.charlemaznable.httpclient.ohclient.OhClient;
import com.github.charlemaznable.httpclient.ohclient.OhFactory;
import io.github.resilience4j.retry.Retry;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RetryTest extends CommonRetryTest {

    @Test
    public void testOhRetry() {
        startMockWebServer();

        val ohLoader = OhFactory.ohLoader(reflectFactory());
        val httpClient = ohLoader.getClient(RetryClient.class);

        countSample.set(0);
        assertEquals("OK", httpClient.getWithConfig());

        countSample.set(0);
        assertThrows(StatusError.class, () ->
                httpClient.getWithParam(null));

        countSample.set(0);
        try {
            httpClient.getWithAnno().get();
        } catch (Exception e) {
            assertTrue(e instanceof ExecutionException);
            assertTrue(e.getCause() instanceof StatusError);
        }

        countSample.set(0);
        try {
            httpClient.getWithDisableConfig().toCompletableFuture().get();
        } catch (Exception e) {
            assertTrue(e instanceof ExecutionException);
            assertTrue(e.getCause() instanceof StatusError);
        }

        shutdownMockWebServer();
    }

    @Mapping("${root}:41440/sample")
    @MappingMethodNameDisabled
    @OhClient
    @ResilienceRetry
    public interface RetryClient {

        @ConfigureWith(CustomRetryConfig.class)
        String getWithConfig();

        String getWithParam(Retry retry);

        @ResilienceRetry(maxAttempts = 2)
        @ResilienceRetry.IsolatedExecutor
        Future<String> getWithAnno();

        @ConfigureWith(DisabledRetryConfig.class)
        CompletionStage<String> getWithDisableConfig();
    }
}
