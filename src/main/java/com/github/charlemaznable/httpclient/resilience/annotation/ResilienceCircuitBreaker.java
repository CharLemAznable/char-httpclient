package com.github.charlemaznable.httpclient.resilience.annotation;

import com.github.charlemaznable.httpclient.resilience.function.ResilienceCircuitBreakerRecover;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Predicate;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_FAILURE_RATE_THRESHOLD;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_MINIMUM_NUMBER_OF_CALLS;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_PERMITTED_CALLS_IN_HALF_OPEN_STATE;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_SLIDING_WINDOW_SIZE;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_SLOW_CALL_DURATION_THRESHOLD;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_SLOW_CALL_RATE_THRESHOLD;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_WAIT_DURATION_IN_HALF_OPEN_STATE;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_WAIT_DURATION_IN_OPEN_STATE;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResilienceCircuitBreaker {

    String name() default "";

    CircuitBreakerConfig.SlidingWindowType slidingWindowType() default CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;

    int slidingWindowSize() default DEFAULT_SLIDING_WINDOW_SIZE;

    int minimumNumberOfCalls() default DEFAULT_MINIMUM_NUMBER_OF_CALLS;

    float failureRateThreshold() default DEFAULT_FAILURE_RATE_THRESHOLD;

    float slowCallRateThreshold() default DEFAULT_SLOW_CALL_RATE_THRESHOLD;

    int slowCallDurationThresholdInSeconds() default DEFAULT_SLOW_CALL_DURATION_THRESHOLD;

    Class<? extends Predicate> recordResultPredicate() default Predicate.class;

    boolean automaticTransitionFromOpenToHalfOpenEnabled() default false;

    int waitDurationInOpenStateInSeconds() default DEFAULT_WAIT_DURATION_IN_OPEN_STATE;

    int permittedNumberOfCallsInHalfOpenState() default DEFAULT_PERMITTED_CALLS_IN_HALF_OPEN_STATE;

    int maxWaitDurationInHalfOpenStateInSeconds() default DEFAULT_WAIT_DURATION_IN_HALF_OPEN_STATE;

    ResilienceCircuitBreakerState state() default ResilienceCircuitBreakerState.ENABLED;

    Class<? extends ResilienceCircuitBreakerRecover> fallback() default ResilienceCircuitBreakerRecover.class;
}
