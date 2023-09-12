package com.github.charlemaznable.httpclient.vxclient.resilience4j;

import com.github.charlemaznable.core.lang.Listt;
import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.annotation.ResilienceCircuitBreaker;
import com.github.charlemaznable.httpclient.common.resilience4j.CommonCircuitBreakerTest;
import com.github.charlemaznable.httpclient.vxclient.VxClient;
import com.github.charlemaznable.httpclient.vxclient.VxFactory;
import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.github.charlemaznable.core.lang.Await.awaitForSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class CircuitBreakerTest extends CommonCircuitBreakerTest {

    @Timeout(value = 1, timeUnit = TimeUnit.MINUTES)
    @Test
    public void testCircuitBreaker(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));
        val httpClient = vxLoader.getClient(CircuitBreakerClient.class);

        errorState.set(true);
        countSample.set(0);
        val getWithConfigs = Listt.<Future<String>>newArrayList();
        for (int i = 0; i < 10; i++) {
            getWithConfigs.add(runQuietly(httpClient.getWithConfig(), test));
        }
        Future.all(getWithConfigs).onComplete(result -> {
            test.verify(() -> assertEquals(10, countSample.get()));

            val getWithConfigsNotPermitted = Listt.<Future<String>>newArrayList();
            for (int i = 0; i < 5; i++) {
                getWithConfigsNotPermitted.add(checkOptionalException(httpClient.getWithConfig(), test));
            }
            Future.all(getWithConfigsNotPermitted).onComplete(resultNotPermitted -> {
                test.verify(() -> assertEquals(10, countSample.get()));

                errorState.set(false);
                awaitForSeconds(10);
                val getWithConfigsHalfOpen = Listt.<Future<String>>newArrayList();
                for (int i = 0; i < 5; i++) {
                    getWithConfigsHalfOpen.add(httpClient.getWithConfig()
                            .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
                }
                Future.all(getWithConfigsHalfOpen).onComplete(resultHalfOpen -> {
                    test.verify(() -> assertEquals(15, countSample.get()));

                    val getWithConfigsNormal = Listt.<Future<String>>newArrayList();
                    for (int i = 0; i < 5; i++) {
                        getWithConfigsNormal.add(httpClient.getWithConfig()
                                .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
                    }
                    Future.all(getWithConfigsNormal).onComplete(resultNormal -> {
                        test.verify(() -> assertEquals(20, countSample.get()));

                        errorState.set(true);
                        countSample.set(0);
                        val getWithParams = Listt.<Future<String>>newArrayList();
                        for (int i = 0; i < 10; i++) {
                            getWithParams.add(runQuietly(httpClient.getWithParam(null), test));
                        }
                        Future.all(getWithParams).onComplete(result2 -> {
                            test.verify(() -> assertEquals(10, countSample.get()));

                            errorState.set(false);
                            val getWithParamsNormal = Listt.<Future<String>>newArrayList();
                            for (int i = 0; i < 5; i++) {
                                getWithParamsNormal.add(httpClient.getWithParam(null)
                                        .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
                            }
                            Future.all(getWithParamsNormal).onComplete(result2Normal -> {
                                test.verify(() -> assertEquals(15, countSample.get()));

                                errorState.set(true);
                                countSample.set(0);
                                val getWithAnnos = Listt.<Future<String>>newArrayList();
                                for (int i = 0; i < 10; i++) {
                                    getWithAnnos.add(runQuietly(httpClient.getWithAnno(), test));
                                }
                                Future.all(getWithAnnos).onComplete(result3 -> {
                                    test.verify(() -> assertEquals(10, countSample.get()));

                                    val getWithAnnosNotPermitted = Listt.<Future<String>>newArrayList();
                                    for (int i = 0; i < 5; i++) {
                                        getWithAnnosNotPermitted.add(checkOptionalException(httpClient.getWithAnno(), test));
                                    }
                                    Future.all(getWithAnnosNotPermitted).onComplete(result3NotPermitted -> {
                                        test.verify(() -> assertEquals(10, countSample.get()));

                                        errorState.set(false);
                                        awaitForSeconds(10);
                                        val getWithAnnosHalfOpen = Listt.<Future<String>>newArrayList();
                                        for (int i = 0; i < 5; i++) {
                                            getWithAnnosHalfOpen.add(httpClient.getWithAnno()
                                                    .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
                                        }
                                        Future.all(getWithAnnosHalfOpen).onComplete(result3HalfOpen -> {
                                            test.verify(() -> assertEquals(15, countSample.get()));

                                            val getWithAnnosNormal = Listt.<Future<String>>newArrayList();
                                            for (int i = 0; i < 5; i++) {
                                                getWithAnnosNormal.add(httpClient.getWithAnno()
                                                        .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
                                            }
                                            Future.all(getWithAnnosNormal).onComplete(result3Normal -> {
                                                test.verify(() -> assertEquals(20, countSample.get()));

                                                errorState.set(true);
                                                countSample.set(0);
                                                val getWithDisableConfigs = Listt.<Future<String>>newArrayList();
                                                for (int i = 0; i < 10; i++) {
                                                    getWithDisableConfigs.add(runQuietly(httpClient.getWithDisableConfig(), test));
                                                }
                                                Future.all(getWithDisableConfigs).onComplete(result4 -> {
                                                    test.verify(() -> assertEquals(10, countSample.get()));

                                                    errorState.set(false);
                                                    val getWithDisableConfigsNormal = Listt.<Future<String>>newArrayList();
                                                    for (int i = 0; i < 5; i++) {
                                                        getWithDisableConfigsNormal.add(httpClient.getWithDisableConfig()
                                                                .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
                                                    }
                                                    Future.all(getWithDisableConfigsNormal).onComplete(result4Normal -> {
                                                        test.verify(() -> assertEquals(15, countSample.get()));

                                                        shutdownMockWebServer();
                                                        test.<CompositeFuture>succeedingThenComplete().handle(result4Normal);
                                                    });
                                                });
                                            });
                                        });
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });
    }

    protected Future<String> runQuietly(Future<String> resultFuture, VertxTestContext test) {
        return resultFuture.otherwise(e ->  null);
    }

    private Future<String> checkOptionalException(Future<String> resultFuture, VertxTestContext test) {
        return resultFuture
                .otherwise(e -> {
                    if (e instanceof ExecutionException) {
                        test.verify(() -> assertTrue(e.getCause() instanceof CallNotPermittedException));
                    } else {
                        test.verify(() -> assertTrue(e instanceof CallNotPermittedException));
                    }
                    return null;
                });
    }

    @Mapping("${root}:41430/sample")
    @MappingMethodNameDisabled
    @VxClient
    @ResilienceCircuitBreaker
    public interface CircuitBreakerClient {

        @ConfigureWith(CustomCircuitBreakerConfig.class)
        Future<String> getWithConfig();

        Future<String> getWithParam(CircuitBreaker circuitBreaker);

        @ResilienceCircuitBreaker(
                slidingWindowSize = 10,
                minimumNumberOfCalls = 10,
                waitDurationInOpenStateInSeconds = 10,
                permittedNumberOfCallsInHalfOpenState = 5
        )
        Future<String> getWithAnno();

        @ConfigureWith(DisabledCircuitBreakerConfig.class)
        Future<String> getWithDisableConfig();
    }
}
