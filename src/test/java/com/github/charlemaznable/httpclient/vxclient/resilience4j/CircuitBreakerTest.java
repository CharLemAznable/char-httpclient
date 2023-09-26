package com.github.charlemaznable.httpclient.vxclient.resilience4j;

import com.github.charlemaznable.core.lang.Listt;
import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.common.StatusError;
import com.github.charlemaznable.httpclient.common.resilience4j.CommonCircuitBreakerTest;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceCircuitBreaker;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceCircuitBreakerState;
import com.github.charlemaznable.httpclient.resilience.common.ResilienceMeterBinder;
import com.github.charlemaznable.httpclient.vxclient.VxClient;
import com.github.charlemaznable.httpclient.vxclient.VxFactory;
import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static com.github.charlemaznable.core.lang.Await.awaitForSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class CircuitBreakerTest extends CommonCircuitBreakerTest {

    @Timeout(value = 1, timeUnit = TimeUnit.MINUTES)
    @Test
    public void testVxCircuitBreaker(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));
        val httpClient = vxLoader.getClient(CircuitBreakerClient.class);
        val httpClient2 = vxLoader.getClient(CircuitBreakerClient2.class);
        val httpClient3 = vxLoader.getClient(CircuitBreakerClient3.class);
        val httpClient4 = vxLoader.getClient(CircuitBreakerClient4.class);

        httpClient.bindTo(new SimpleMeterRegistry());

        errorState.set(true);
        countSample.set(0);
        val getWithConfigs = Listt.<Future<String>>newArrayList();
        for (int i = 0; i < 10; i++) {
            getWithConfigs.add(runQuietly(httpClient.getWithConfig(), test));
        }
        Future.all(getWithConfigs).compose(result -> {
            test.verify(() -> assertEquals(10, countSample.get()));

            val getWithConfigsNotPermitted = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 5; i++) {
                getWithConfigsNotPermitted.add(httpClient.getWithConfig()
                        .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
            }
            return Future.all(getWithConfigsNotPermitted);
        }).compose(result -> {
            test.verify(() -> assertEquals(10, countSample.get()));

            errorState.set(false);
            awaitForSeconds(10);
            val getWithConfigsHalfOpen = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 5; i++) {
                getWithConfigsHalfOpen.add(httpClient.getWithConfig()
                        .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
            }
            return Future.all(getWithConfigsHalfOpen);
        }).compose(result -> {
            test.verify(() -> assertEquals(15, countSample.get()));

            val getWithConfigsNormal = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 5; i++) {
                getWithConfigsNormal.add(httpClient.getWithConfig()
                        .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
            }
            return Future.all(getWithConfigsNormal);
        }).compose(result -> {
            test.verify(() -> assertEquals(20, countSample.get()));

            errorState.set(true);
            countSample.set(0);
            val getWithParams = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 10; i++) {
                getWithParams.add(runQuietly(httpClient.getWithParam(null), test));
            }
            return Future.all(getWithParams);
        }).compose(result -> {
            test.verify(() -> assertEquals(10, countSample.get()));

            errorState.set(false);
            val getWithParamsNormal = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 5; i++) {
                getWithParamsNormal.add(httpClient.getWithParam(null)
                        .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
            }
            return Future.all(getWithParamsNormal);
        }).compose(result -> {
            test.verify(() -> assertEquals(15, countSample.get()));

            httpClient.bindTo(null);

            errorState.set(true);
            countSample.set(0);
            val getWithAnnos = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 10; i++) {
                getWithAnnos.add(runQuietly(httpClient.getWithAnno(), test));
            }
            return Future.all(getWithAnnos);
        }).compose(result -> {
            test.verify(() -> assertEquals(10, countSample.get()));

            val getWithAnnosNotPermitted = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 5; i++) {
                getWithAnnosNotPermitted.add(httpClient.getWithAnno()
                        .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
            }
            return Future.all(getWithAnnosNotPermitted);
        }).compose(result -> {
            test.verify(() -> assertEquals(10, countSample.get()));

            errorState.set(false);
            awaitForSeconds(10);
            val getWithAnnosHalfOpen = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 5; i++) {
                getWithAnnosHalfOpen.add(httpClient.getWithAnno()
                        .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
            }
            return Future.all(getWithAnnosHalfOpen);
        }).compose(result -> {
            test.verify(() -> assertEquals(15, countSample.get()));

            val getWithAnnosNormal = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 5; i++) {
                getWithAnnosNormal.add(httpClient.getWithAnno()
                        .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
            }
            return Future.all(getWithAnnosNormal);
        }).compose(result -> {
            test.verify(() -> assertEquals(20, countSample.get()));

            errorState.set(true);
            countSample.set(0);
            val getWithDisableConfigs = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 10; i++) {
                getWithDisableConfigs.add(runQuietly(httpClient.getWithDisableConfig(), test));
            }
            return Future.all(getWithDisableConfigs);
        }).compose(result -> {
            test.verify(() -> assertEquals(10, countSample.get()));

            errorState.set(false);
            val getWithDisableConfigsNormal = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 5; i++) {
                getWithDisableConfigsNormal.add(httpClient.getWithDisableConfig()
                        .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
            }
            return Future.all(getWithDisableConfigsNormal);
        }).compose(result -> {
            test.verify(() -> assertEquals(15, countSample.get()));

            errorState.set(true);
            countSample.set(0);
            val getWithConfigs2 = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 10; i++) {
                getWithConfigs2.add(runQuietly(httpClient2.getWithConfig(), test));
            }
            return Future.all(getWithConfigs2);
        }).compose(result -> {
            test.verify(() -> assertEquals(10, countSample.get()));

            val getWithConfigs2NotPermitted = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 5; i++) {
                getWithConfigs2NotPermitted.add(httpClient2.getWithConfig()
                        .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
            }
            return Future.all(getWithConfigs2NotPermitted);
        }).compose(result -> {
            test.verify(() -> assertEquals(10, countSample.get()));

            errorState.set(false);
            awaitForSeconds(10);
            val getWithConfigs2HalfOpen = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 5; i++) {
                getWithConfigs2HalfOpen.add(httpClient2.getWithConfig()
                        .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
            }
            return Future.all(getWithConfigs2HalfOpen);
        }).compose(result -> {
            test.verify(() -> assertEquals(15, countSample.get()));

            val getWithConfigs2Normal = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 5; i++) {
                getWithConfigs2Normal.add(httpClient2.getWithConfig()
                        .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
            }
            return Future.all(getWithConfigs2Normal);
        }).compose(result -> {
            test.verify(() -> assertEquals(20, countSample.get()));

            errorState.set(true);
            countSample.set(0);
            val getWithAnnos2 = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 10; i++) {
                getWithAnnos2.add(runQuietly(httpClient2.getWithAnno(), test));
            }
            return Future.all(getWithAnnos2);
        }).compose(result -> {
            test.verify(() -> assertEquals(10, countSample.get()));

            val getWithAnnos2NotPermitted = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 5; i++) {
                getWithAnnos2NotPermitted.add(httpClient2.getWithAnno()
                        .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
            }
            return Future.all(getWithAnnos2NotPermitted);
        }).compose(result -> {
            test.verify(() -> assertEquals(10, countSample.get()));

            errorState.set(false);
            awaitForSeconds(10);
            val getWithAnnos2HalfOpen = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 5; i++) {
                getWithAnnos2HalfOpen.add(httpClient2.getWithAnno()
                        .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
            }
            return Future.all(getWithAnnos2HalfOpen);
        }).compose(result -> {
            test.verify(() -> assertEquals(15, countSample.get()));

            val getWithAnnos2Normal = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 5; i++) {
                getWithAnnos2Normal.add(httpClient2.getWithAnno()
                        .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
            }
            return Future.all(getWithAnnos2Normal);
        }).compose(result -> {
            test.verify(() -> assertEquals(20, countSample.get()));

            AllpassCircuitBreakerConfig.state = ResilienceCircuitBreakerState.DISABLED.name();
            httpClient3.reload();
            val gets = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 10; i++) {
                gets.add(httpClient3.get().otherwise(exception -> {
                    assertTrue(exception instanceof StatusError);
                    return null;
                }));
            }
            return Future.all(gets);
        }).compose(result -> {
            AllpassCircuitBreakerConfig.state = ResilienceCircuitBreakerState.METRICS_ONLY.name();
            httpClient3.reload();
            val gets = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 10; i++) {
                gets.add(httpClient3.get().otherwise(exception -> {
                    assertTrue(exception instanceof StatusError);
                    return null;
                }));
            }
            return Future.all(gets);
        }).compose(result -> {
            val gets = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 10; i++) {
                gets.add(httpClient4.get().otherwise(exception -> {
                    assertTrue(exception instanceof CallNotPermittedException);
                    return null;
                }));
            }
            return Future.all(gets);
        }).compose(result -> {
            shutdownMockWebServer();
            return Future.succeededFuture();
        }).onComplete(test.succeedingThenComplete());
    }

    protected Future<String> runQuietly(Future<String> resultFuture, VertxTestContext test) {
        return resultFuture.otherwise(e -> null);
    }

    @Mapping("${root}:41430/sample")
    @MappingMethodNameDisabled
    @VxClient
    @ConfigureWith(DefaultCircuitBreakerConfig.class)
    public interface CircuitBreakerClient extends ResilienceMeterBinder {

        @ConfigureWith(CustomCircuitBreakerConfig.class)
        Future<String> getWithConfig();

        Future<String> getWithParam(CircuitBreaker circuitBreaker);

        @ResilienceCircuitBreaker(
                slidingWindowSize = 10,
                minimumNumberOfCalls = 10,
                waitDurationInOpenStateInSeconds = 10,
                permittedNumberOfCallsInHalfOpenState = 5,
                fallback = CustomResilienceCircuitBreakerRecover.class)
        Future<String> getWithAnno();

        @ConfigureWith(DisabledCircuitBreakerConfig.class)
        Future<String> getWithDisableConfig();
    }

    @Mapping("${root}:41430/sample2")
    @MappingMethodNameDisabled
    @VxClient
    @ResilienceCircuitBreaker
    public interface CircuitBreakerClient2 {

        @ConfigureWith(CustomCircuitBreakerConfig.class)
        Future<String> getWithConfig();

        @ResilienceCircuitBreaker(
                slidingWindowSize = 10,
                minimumNumberOfCalls = 10,
                recordResultPredicate = CustomResultPredicate.class,
                waitDurationInOpenStateInSeconds = 10,
                permittedNumberOfCallsInHalfOpenState = 5,
                fallback = CustomResilienceCircuitBreakerRecover.class)
        Future<String> getWithAnno();
    }

    @Mapping("${root}:41430/sample3")
    @MappingMethodNameDisabled
    @VxClient
    @ConfigureWith(AllpassCircuitBreakerConfig.class)
    public interface CircuitBreakerClient3 extends Reloadable {

        Future<String> get();
    }

    @Mapping("${root}:41430/sample4")
    @MappingMethodNameDisabled
    @VxClient
    @ResilienceCircuitBreaker(
            slidingWindowSize = 5,
            minimumNumberOfCalls = 5,
            state = ResilienceCircuitBreakerState.FORCED_OPEN)
    public interface CircuitBreakerClient4 extends Reloadable {

        Future<String> get();
    }
}
