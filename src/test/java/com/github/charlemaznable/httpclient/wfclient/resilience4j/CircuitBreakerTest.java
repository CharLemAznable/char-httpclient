package com.github.charlemaznable.httpclient.wfclient.resilience4j;

import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.common.StatusError;
import com.github.charlemaznable.httpclient.common.resilience4j.CommonCircuitBreakerTest;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceCircuitBreaker;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceCircuitBreakerState;
import com.github.charlemaznable.httpclient.common.MeterBinder;
import com.github.charlemaznable.httpclient.wfclient.WfClient;
import com.github.charlemaznable.httpclient.wfclient.WfFactory;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Await.awaitForSeconds;
import static org.jooq.lambda.Sneaky.runnable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CircuitBreakerTest extends CommonCircuitBreakerTest {

    @SneakyThrows
    @Test
    public void testWfCircuitBreaker() {
        startMockWebServer();

        val wfLoader = WfFactory.wfLoader(reflectFactory());
        val httpClient = wfLoader.getClient(CircuitBreakerClient.class);
        val httpClient2 = wfLoader.getClient(CircuitBreakerClient2.class);
        val httpClient3 = wfLoader.getClient(CircuitBreakerClient3.class);
        val httpClient4 = wfLoader.getClient(CircuitBreakerClient4.class);

        httpClient.bindTo(new SimpleMeterRegistry());

        errorState.set(true);
        countSample.set(0);
        val service = new Thread[10];
        for (int i = 0; i < 10; i++) {
            service[i] = new Thread(runQuietly(() -> httpClient.getWithConfig().block()), "service" + i);
            service[i].start();
        }
        for (int i = 0; i < 10; i++) {
            service[i].join();
        }
        assertEquals(10, countSample.get());
        val serviceNotPermitted = new Thread[5];
        for (int i = 0; i < 5; i++) {
            serviceNotPermitted[i] = new Thread(() -> assertEquals("OK", httpClient.getWithConfig().block()), "serviceNotPermitted" + i);
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
            serviceHalfOpen[i] = new Thread(() -> assertEquals("OK", httpClient.getWithConfig().block()), "serviceHalfOpen" + i);
            serviceHalfOpen[i].start();
        }
        for (int i = 0; i < 5; i++) {
            serviceHalfOpen[i].join();
        }
        assertEquals(15, countSample.get());
        val serviceNormal = new Thread[5];
        for (int i = 0; i < 5; i++) {
            serviceNormal[i] = new Thread(() -> assertEquals("OK", httpClient.getWithConfig().block()), "serviceNormal" + i);
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
            service2[i] = new Thread(runQuietly(() -> httpClient.getWithParam(null).block()), "service2" + i);
            service2[i].start();
        }
        for (int i = 0; i < 10; i++) {
            service2[i].join();
        }
        assertEquals(10, countSample.get());
        errorState.set(false);
        val service2Normal = new Thread[5];
        for (int i = 0; i < 5; i++) {
            service2Normal[i] = new Thread(() -> assertEquals("OK", httpClient.getWithParam(null).block()), "service2Normal" + i);
            service2Normal[i].start();
        }
        for (int i = 0; i < 5; i++) {
            service2Normal[i].join();
        }
        assertEquals(15, countSample.get());

        httpClient.bindTo(null);

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

        errorState.set(true);
        countSample.set(0);
        val service5 = new Thread[10];
        for (int i = 0; i < 10; i++) {
            service5[i] = new Thread(() -> assertDoesNotThrow(() -> httpClient2.getWithConfig().block()), "service5" + i);
            service5[i].start();
        }
        for (int i = 0; i < 10; i++) {
            service5[i].join();
        }
        assertEquals(10, countSample.get());
        val service5NotPermitted = new Thread[5];
        for (int i = 0; i < 5; i++) {
            service5NotPermitted[i] = new Thread(() -> assertEquals("OK", httpClient2.getWithConfig().block()), "service5NotPermitted" + i);
            service5NotPermitted[i].start();
        }
        for (int i = 0; i < 5; i++) {
            service5NotPermitted[i].join();
        }
        assertEquals(10, countSample.get());
        errorState.set(false);
        awaitForSeconds(10);
        val service5HalfOpen = new Thread[5];
        for (int i = 0; i < 5; i++) {
            service5HalfOpen[i] = new Thread(() -> assertEquals("OK", httpClient2.getWithConfig().block()), "service5HalfOpen" + i);
            service5HalfOpen[i].start();
        }
        for (int i = 0; i < 5; i++) {
            service5HalfOpen[i].join();
        }
        assertEquals(15, countSample.get());
        val service5Normal = new Thread[5];
        for (int i = 0; i < 5; i++) {
            service5Normal[i] = new Thread(() -> assertEquals("OK", httpClient2.getWithConfig().block()), "service5Normal" + i);
            service5Normal[i].start();
        }
        for (int i = 0; i < 5; i++) {
            service5Normal[i].join();
        }
        assertEquals(20, countSample.get());

        errorState.set(true);
        countSample.set(0);
        val service6 = new Thread[10];
        for (int i = 0; i < 10; i++) {
            service6[i] = new Thread(() -> assertDoesNotThrow(() -> httpClient2.getWithAnno().block()), "service6" + i);
            service6[i].start();
        }
        for (int i = 0; i < 10; i++) {
            service6[i].join();
        }
        assertEquals(10, countSample.get());
        val service6NotPermitted = new Thread[5];
        for (int i = 0; i < 5; i++) {
            service6NotPermitted[i] = new Thread(() -> assertEquals("OK", httpClient2.getWithAnno().block()), "service6NotPermitted" + i);
            service6NotPermitted[i].start();
        }
        for (int i = 0; i < 5; i++) {
            service6NotPermitted[i].join();
        }
        assertEquals(10, countSample.get());
        errorState.set(false);
        awaitForSeconds(10);
        val service6HalfOpen = new Thread[5];
        for (int i = 0; i < 5; i++) {
            service6HalfOpen[i] = new Thread(() -> assertEquals("OK", httpClient2.getWithAnno().block()), "service6HalfOpen" + i);
            service6HalfOpen[i].start();
        }
        for (int i = 0; i < 5; i++) {
            service6HalfOpen[i].join();
        }
        assertEquals(15, countSample.get());
        val service6Normal = new Thread[5];
        for (int i = 0; i < 5; i++) {
            service6Normal[i] = new Thread(() -> assertEquals("OK", httpClient2.getWithAnno().block()), "service6Normal" + i);
            service6Normal[i].start();
        }
        for (int i = 0; i < 5; i++) {
            service6Normal[i].join();
        }
        assertEquals(20, countSample.get());

        AllpassCircuitBreakerConfig.state = ResilienceCircuitBreakerState.DISABLED.name();
        httpClient3.reload();
        val service7 = new Thread[10];
        for (int i = 0; i < 10; i++) {
            service7[i] = new Thread(() -> assertThrows(StatusError.class, () -> httpClient3.get().block()), "service7" + i);
            service7[i].start();
        }
        for (int i = 0; i < 10; i++) {
            service7[i].join();
        }

        AllpassCircuitBreakerConfig.state = ResilienceCircuitBreakerState.METRICS_ONLY.name();
        httpClient3.reload();
        val service8 = new Thread[10];
        for (int i = 0; i < 10; i++) {
            service8[i] = new Thread(() -> assertThrows(StatusError.class, () -> httpClient3.get().block()), "service8" + i);
            service8[i].start();
        }
        for (int i = 0; i < 10; i++) {
            service8[i].join();
        }

        val service9 = new Thread[10];
        for (int i = 0; i < 10; i++) {
            service9[i] = new Thread(() -> assertThrows(CallNotPermittedException.class, () -> httpClient4.get().block()), "service9" + i);
            service9[i].start();
        }
        for (int i = 0; i < 10; i++) {
            service9[i].join();
        }

        shutdownMockWebServer();
    }

    @Mapping("${root}:41430/sample")
    @MappingMethodNameDisabled
    @WfClient
    @ConfigureWith(DefaultCircuitBreakerConfig.class)
    public interface CircuitBreakerClient extends MeterBinder {

        @ConfigureWith(CustomCircuitBreakerConfig.class)
        Mono<String> getWithConfig();

        Mono<String> getWithParam(CircuitBreaker circuitBreaker);

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

    @Mapping("${root}:41430/sample2")
    @MappingMethodNameDisabled
    @WfClient
    @ResilienceCircuitBreaker
    public interface CircuitBreakerClient2 {

        @ConfigureWith(CustomCircuitBreakerConfig.class)
        Mono<String> getWithConfig();

        @ResilienceCircuitBreaker(
                slidingWindowSize = 10,
                minimumNumberOfCalls = 10,
                recordResultPredicate = CustomResultPredicate.class,
                waitDurationInOpenStateInSeconds = 10,
                permittedNumberOfCallsInHalfOpenState = 5,
                fallback = CustomResilienceCircuitBreakerRecover.class)
        Mono<String> getWithAnno();
    }

    @Mapping("${root}:41430/sample3")
    @MappingMethodNameDisabled
    @WfClient
    @ConfigureWith(AllpassCircuitBreakerConfig.class)
    public interface CircuitBreakerClient3 extends Reloadable {

        Mono<String> get();
    }

    @Mapping("${root}:41430/sample4")
    @MappingMethodNameDisabled
    @WfClient
    @ResilienceCircuitBreaker(
            slidingWindowSize = 5,
            minimumNumberOfCalls = 5,
            state = ResilienceCircuitBreakerState.FORCED_OPEN)
    public interface CircuitBreakerClient4 extends Reloadable {

        Mono<String> get();
    }
}
