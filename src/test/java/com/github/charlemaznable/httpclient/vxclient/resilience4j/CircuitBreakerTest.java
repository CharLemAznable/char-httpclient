package com.github.charlemaznable.httpclient.vxclient.resilience4j;

import com.github.charlemaznable.core.lang.Listt;
import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.common.resilience4j.CommonCircuitBreakerTest;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceCircuitBreaker;
import com.github.charlemaznable.httpclient.resilience.common.ResilienceMeterBinder;
import com.github.charlemaznable.httpclient.vxclient.VxClient;
import com.github.charlemaznable.httpclient.vxclient.VxFactory;
import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.vertx.core.CompositeFuture;
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

@ExtendWith(VertxExtension.class)
public class CircuitBreakerTest extends CommonCircuitBreakerTest {

    @Timeout(value = 1, timeUnit = TimeUnit.MINUTES)
    @Test
    public void testVxCircuitBreaker(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));
        val httpClient = vxLoader.getClient(CircuitBreakerClient.class);
        httpClient.bindTo(new SimpleMeterRegistry());

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
                getWithConfigsNotPermitted.add(httpClient.getWithConfig()
                        .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
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

                                httpClient.bindTo(null);

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
                                        getWithAnnosNotPermitted.add(httpClient.getWithAnno()
                                                .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
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

                                                        val httpClient2 = vxLoader.getClient(CircuitBreakerClient2.class);

                                                        errorState.set(true);
                                                        countSample.set(0);
                                                        val getWithConfigs2 = Listt.<Future<String>>newArrayList();
                                                        for (int i = 0; i < 10; i++) {
                                                            getWithConfigs2.add(runQuietly(httpClient2.getWithConfig(), test));
                                                        }
                                                        Future.all(getWithConfigs2).onComplete(result5 -> {
                                                            test.verify(() -> assertEquals(10, countSample.get()));

                                                            val getWithConfigs2NotPermitted = Listt.<Future<String>>newArrayList();
                                                            for (int i = 0; i < 5; i++) {
                                                                getWithConfigs2NotPermitted.add(httpClient2.getWithConfig()
                                                                        .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
                                                            }
                                                            Future.all(getWithConfigs2NotPermitted).onComplete(result5NotPermitted -> {
                                                                test.verify(() -> assertEquals(10, countSample.get()));

                                                                errorState.set(false);
                                                                awaitForSeconds(10);
                                                                val getWithConfigs2HalfOpen = Listt.<Future<String>>newArrayList();
                                                                for (int i = 0; i < 5; i++) {
                                                                    getWithConfigs2HalfOpen.add(httpClient2.getWithConfig()
                                                                            .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
                                                                }
                                                                Future.all(getWithConfigs2HalfOpen).onComplete(result5HalfOpen -> {
                                                                    test.verify(() -> assertEquals(15, countSample.get()));

                                                                    val getWithConfigs2Normal = Listt.<Future<String>>newArrayList();
                                                                    for (int i = 0; i < 5; i++) {
                                                                        getWithConfigs2Normal.add(httpClient2.getWithConfig()
                                                                                .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
                                                                    }
                                                                    Future.all(getWithConfigs2Normal).onComplete(result5Normal -> {
                                                                        test.verify(() -> assertEquals(20, countSample.get()));

                                                                        errorState.set(true);
                                                                        countSample.set(0);
                                                                        val getWithAnnos2 = Listt.<Future<String>>newArrayList();
                                                                        for (int i = 0; i < 10; i++) {
                                                                            getWithAnnos2.add(runQuietly(httpClient2.getWithAnno(), test));
                                                                        }
                                                                        Future.all(getWithAnnos2).onComplete(result6 -> {
                                                                            test.verify(() -> assertEquals(10, countSample.get()));

                                                                            val getWithAnnos2NotPermitted = Listt.<Future<String>>newArrayList();
                                                                            for (int i = 0; i < 5; i++) {
                                                                                getWithAnnos2NotPermitted.add(httpClient2.getWithAnno()
                                                                                        .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
                                                                            }
                                                                            Future.all(getWithAnnos2NotPermitted).onComplete(result6NotPermitted -> {
                                                                                test.verify(() -> assertEquals(10, countSample.get()));

                                                                                errorState.set(false);
                                                                                awaitForSeconds(10);
                                                                                val getWithAnnos2HalfOpen = Listt.<Future<String>>newArrayList();
                                                                                for (int i = 0; i < 5; i++) {
                                                                                    getWithAnnos2HalfOpen.add(httpClient2.getWithAnno()
                                                                                            .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
                                                                                }
                                                                                Future.all(getWithAnnos2HalfOpen).onComplete(result6HalfOpen -> {
                                                                                    test.verify(() -> assertEquals(15, countSample.get()));

                                                                                    val getWithAnnos2Normal = Listt.<Future<String>>newArrayList();
                                                                                    for (int i = 0; i < 5; i++) {
                                                                                        getWithAnnos2Normal.add(httpClient2.getWithAnno()
                                                                                                .onSuccess(response -> test.verify(() -> assertEquals("OK", response))));
                                                                                    }
                                                                                    Future.all(getWithAnnos2Normal).onComplete(result6Normal -> {
                                                                                        test.verify(() -> assertEquals(20, countSample.get()));

                                                                                        shutdownMockWebServer();
                                                                                        test.<CompositeFuture>succeedingThenComplete().handle(result6Normal);
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
}
