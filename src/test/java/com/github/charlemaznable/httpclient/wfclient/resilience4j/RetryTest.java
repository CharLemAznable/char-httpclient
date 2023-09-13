package com.github.charlemaznable.httpclient.wfclient.resilience4j;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.annotation.ResilienceRetry;
import com.github.charlemaznable.httpclient.common.StatusError;
import com.github.charlemaznable.httpclient.common.resilience4j.CommonRetryTest;
import com.github.charlemaznable.httpclient.wfclient.WfClient;
import com.github.charlemaznable.httpclient.wfclient.WfFactory;
import io.github.resilience4j.retry.Retry;
import lombok.val;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RetryTest extends CommonRetryTest {

    @Test
    public void testWfRetry() {
        startMockWebServer();

        val wfLoader = WfFactory.wfLoader(reflectFactory());
        val httpClient = wfLoader.getClient(RetryClient.class);

        countSample.set(0);
        assertEquals("OK", httpClient.getWithConfig().block());

        countSample.set(0);
        assertThrows(StatusError.class, () ->
                httpClient.getWithParam(null).block());

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
    @WfClient
    @ResilienceRetry
    public interface RetryClient {

        @ConfigureWith(CustomRetryConfig.class)
        Mono<String> getWithConfig();

        Mono<String> getWithParam(Retry retry);

        @ResilienceRetry(maxAttempts = 2)
        @ResilienceRetry.IsolatedExecutor
        Future<String> getWithAnno();

        @ConfigureWith(DisabledRetryConfig.class)
        CompletionStage<String> getWithDisableConfig();
    }
}