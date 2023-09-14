package com.github.charlemaznable.httpclient.ohclient.resilience4j;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.annotation.ResilienceCircuitBreaker;
import com.github.charlemaznable.httpclient.common.resilience4j.CommonCircuitBreakerTest;
import com.github.charlemaznable.httpclient.ohclient.OhClient;
import com.github.charlemaznable.httpclient.ohclient.OhFactory;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Await.awaitForSeconds;
import static org.jooq.lambda.Sneaky.runnable;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CircuitBreakerTest extends CommonCircuitBreakerTest {

    @SneakyThrows
    @Test
    public void testOhCircuitBreaker() {
        startMockWebServer();

        val ohLoader = OhFactory.ohLoader(reflectFactory());
        val httpClient = ohLoader.getClient(CircuitBreakerClient.class);

        errorState.set(true);
        countSample.set(0);
        val service = new Thread[10];
        for (int i = 0; i < 10; i++) {
            service[i] = new Thread(runQuietly(httpClient::getWithConfig), "service" + i);
            service[i].start();
        }
        for (int i = 0; i < 10; i++) {
            service[i].join();
        }
        assertEquals(10, countSample.get());
        val serviceNotPermitted = new Thread[5];
        for (int i = 0; i < 5; i++) {
            serviceNotPermitted[i] = new Thread(() -> assertEquals("OK", httpClient.getWithConfig()), "serviceNotPermitted" + i);
            serviceNotPermitted[i].start();
        }
        for (int i = 0; i < 5; i++) {
            serviceNotPermitted[i].join();
        }
        assertEquals(10, countSample.get());
        errorState.set(false);
        awaitForSeconds(10);
        val serviceHalfOpen = new Thread[5];
        for (int i = 0; i < 5; i++) {
            serviceHalfOpen[i] = new Thread(() -> assertEquals("OK", httpClient.getWithConfig()), "serviceHalfOpen" + i);
            serviceHalfOpen[i].start();
        }
        for (int i = 0; i < 5; i++) {
            serviceHalfOpen[i].join();
        }
        assertEquals(15, countSample.get());
        val serviceNormal = new Thread[5];
        for (int i = 0; i < 5; i++) {
            serviceNormal[i] = new Thread(() -> assertEquals("OK", httpClient.getWithConfig()), "serviceNormal" + i);
            serviceNormal[i].start();
        }
        for (int i = 0; i < 5; i++) {
            serviceNormal[i].join();
        }
        assertEquals(20, countSample.get());

        errorState.set(true);
        countSample.set(0);
        val service2 = new Thread[10];
        for (int i = 0; i < 10; i++) {
            service2[i] = new Thread(runQuietly(() -> httpClient.getWithParam(null)), "service2" + i);
            service2[i].start();
        }
        for (int i = 0; i < 10; i++) {
            service2[i].join();
        }
        assertEquals(10, countSample.get());
        errorState.set(false);
        val service2Normal = new Thread[5];
        for (int i = 0; i < 5; i++) {
            service2Normal[i] = new Thread(() -> assertEquals("OK", httpClient.getWithParam(null)), "service2Normal" + i);
            service2Normal[i].start();
        }
        for (int i = 0; i < 5; i++) {
            service2Normal[i].join();
        }
        assertEquals(15, countSample.get());

        errorState.set(true);
        countSample.set(0);
        val service3 = new Thread[10];
        for (int i = 0; i < 10; i++) {
            service3[i] = new Thread(runQuietly(() -> httpClient.getWithAnno().get()), "service3" + i);
            service3[i].start();
        }
        for (int i = 0; i < 10; i++) {
            service3[i].join();
        }
        assertEquals(10, countSample.get());
        val service3NotPermitted = new Thread[5];
        for (int i = 0; i < 5; i++) {
            service3NotPermitted[i] = new Thread(runnable(() -> assertEquals("OK", httpClient.getWithAnno().get())), "service3NotPermitted" + i);
            service3NotPermitted[i].start();
        }
        for (int i = 0; i < 5; i++) {
            service3NotPermitted[i].join();
        }
        assertEquals(10, countSample.get());
        errorState.set(false);
        awaitForSeconds(10);
        val service3HalfOpen = new Thread[5];
        for (int i = 0; i < 5; i++) {
            service3HalfOpen[i] = new Thread(runnable(() -> assertEquals("OK", httpClient.getWithAnno().get())), "service3HalfOpen" + i);
            service3HalfOpen[i].start();
        }
        for (int i = 0; i < 5; i++) {
            service3HalfOpen[i].join();
        }
        assertEquals(15, countSample.get());
        val service3Normal = new Thread[5];
        for (int i = 0; i < 5; i++) {
            service3Normal[i] = new Thread(runnable(() -> assertEquals("OK", httpClient.getWithAnno().get())), "service3Normal" + i);
            service3Normal[i].start();
        }
        for (int i = 0; i < 5; i++) {
            service3Normal[i].join();
        }
        assertEquals(20, countSample.get());

        errorState.set(true);
        countSample.set(0);
        val service4 = new Thread[10];
        for (int i = 0; i < 10; i++) {
            service4[i] = new Thread(runQuietly(() -> httpClient.getWithDisableConfig().toCompletableFuture().get()), "service4" + i);
            service4[i].start();
        }
        for (int i = 0; i < 10; i++) {
            service4[i].join();
        }
        assertEquals(10, countSample.get());
        errorState.set(false);
        val service4Normal = new Thread[5];
        for (int i = 0; i < 5; i++) {
            service4Normal[i] = new Thread(runnable(() -> assertEquals("OK", httpClient.getWithDisableConfig().toCompletableFuture().get())), "service4Normal" + i);
            service4Normal[i].start();
        }
        for (int i = 0; i < 5; i++) {
            service4Normal[i].join();
        }
        assertEquals(15, countSample.get());

        shutdownMockWebServer();
    }

    @Mapping("${root}:41430/sample")
    @MappingMethodNameDisabled
    @OhClient
    @ConfigureWith(DefaultCircuitBreakerConfig.class)
    public interface CircuitBreakerClient {

        @ConfigureWith(CustomCircuitBreakerConfig.class)
        String getWithConfig();

        String getWithParam(CircuitBreaker circuitBreaker);

        @ResilienceCircuitBreaker(
                slidingWindowSize = 10,
                minimumNumberOfCalls = 10,
                waitDurationInOpenStateInSeconds = 10,
                permittedNumberOfCallsInHalfOpenState = 5,
                fallback = CustomResilienceCircuitBreakerRecover.class)
        Future<String> getWithAnno();

        @ConfigureWith(DisabledCircuitBreakerConfig.class)
        CompletionStage<String> getWithDisableConfig();
    }
}
