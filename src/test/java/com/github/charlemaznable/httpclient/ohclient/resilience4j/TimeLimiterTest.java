package com.github.charlemaznable.httpclient.ohclient.resilience4j;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.common.resilience4j.CommonTimeLimiterTest;
import com.github.charlemaznable.httpclient.ohclient.OhClient;
import com.github.charlemaznable.httpclient.ohclient.OhFactory;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceTimeLimiter;
import com.github.charlemaznable.httpclient.resilience.common.ResilienceMeterBinder;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimeLimiterTest extends CommonTimeLimiterTest {

    @SneakyThrows
    @Test
    public void testOhTimeLimiter() {
        startMockWebServer();

        val ohLoader = OhFactory.ohLoader(reflectFactory());
        val httpClient = ohLoader.getClient(TimeLimiterClient.class);
        httpClient.bindTo(new SimpleMeterRegistry());

        assertEquals("Timeout", httpClient.getWithConfig());

        assertEquals("OK", httpClient.getWithParam(null));

        httpClient.bindTo(null);

        assertEquals("Timeout", httpClient.getWithAnno().get());

        assertEquals("OK", httpClient.getWithDisableConfig().toCompletableFuture().get());

        shutdownMockWebServer();
    }

    @Mapping("${root}:41450/sample")
    @MappingMethodNameDisabled
    @OhClient
    @ConfigureWith(DefaultTimeLimiterConfig.class)
    public interface TimeLimiterClient extends ResilienceMeterBinder {

        @ConfigureWith(CustomTimeLimiterConfig.class)
        String getWithConfig();

        String getWithParam(TimeLimiter timeLimiter);

        @ResilienceTimeLimiter(
                fallback = CustomResilienceTimeLimiterRecover.class)
        Future<String> getWithAnno();

        @ConfigureWith(DisabledTimeLimiterConfig.class)
        CompletionStage<String> getWithDisableConfig();
    }
}
