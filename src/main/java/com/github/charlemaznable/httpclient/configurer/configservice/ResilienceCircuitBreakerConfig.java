package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.configurer.ResilienceCircuitBreakerConfigurer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import lombok.val;

import java.time.Duration;

import static com.github.charlemaznable.configservice.impl.Functions.TO_BOOLEAN_FUNCTION;
import static com.github.charlemaznable.configservice.impl.Functions.TO_DURATION_FUNCTION;
import static com.github.charlemaznable.configservice.impl.Functions.TO_FLOAT_FUNCTION;
import static com.github.charlemaznable.configservice.impl.Functions.TO_INT_FUNCTION;
import static com.github.charlemaznable.core.lang.Str.isBlank;
import static com.github.charlemaznable.httpclient.configurer.configservice.ConfigurerElf.parseStringToValue;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_FAILURE_RATE_THRESHOLD;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_MINIMUM_NUMBER_OF_CALLS;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_PERMITTED_CALLS_IN_HALF_OPEN_STATE;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_SLIDING_WINDOW_SIZE;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_SLOW_CALL_DURATION_THRESHOLD;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_SLOW_CALL_RATE_THRESHOLD;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_WAIT_DURATION_IN_HALF_OPEN_STATE;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_WAIT_DURATION_IN_OPEN_STATE;

public interface ResilienceCircuitBreakerConfig extends ResilienceCircuitBreakerConfigurer {

    @Config("circuitBreakerName")
    String circuitBreakerName();

    @Config("slidingWindowType")
    String slidingWindowType();

    default CircuitBreakerConfig.SlidingWindowType parseSlidingWindowType() {
        return parseStringToValue(slidingWindowType(),
                CircuitBreakerConfig.SlidingWindowType.COUNT_BASED,
                CircuitBreakerConfig.SlidingWindowType::valueOf);
    }

    @Config("slidingWindowSize")
    String slidingWindowSize();

    default int parseSlidingWindowSize() {
        return parseStringToValue(slidingWindowSize(),
                DEFAULT_SLIDING_WINDOW_SIZE, TO_INT_FUNCTION);
    }

    @Config("minimumNumberOfCalls")
    String minimumNumberOfCalls();

    default int parseMinimumNumberOfCalls() {
        return parseStringToValue(minimumNumberOfCalls(),
                DEFAULT_MINIMUM_NUMBER_OF_CALLS, TO_INT_FUNCTION);
    }

    @Config("failureRateThreshold")
    String failureRateThreshold();

    default float parseFailureRateThreshold() {
        return parseStringToValue(failureRateThreshold(),
                (float) DEFAULT_FAILURE_RATE_THRESHOLD, TO_FLOAT_FUNCTION);
    }

    @Config("slowCallRateThreshold")
    String slowCallRateThreshold();

    default float parseSlowCallRateThreshold() {
        return parseStringToValue(slowCallRateThreshold(),
                (float) DEFAULT_SLOW_CALL_RATE_THRESHOLD, TO_FLOAT_FUNCTION);
    }

    @Config("slowCallDurationThreshold")
    String slowCallDurationThreshold();

    default Duration parseSlowCallDurationThreshold() {
        return parseStringToValue(slowCallDurationThreshold(),
                Duration.ofSeconds(DEFAULT_SLOW_CALL_DURATION_THRESHOLD),
                TO_DURATION_FUNCTION.andThen(Duration::ofMillis));
    }

    @Config("automaticTransitionFromOpenToHalfOpenEnabled")
    String automaticTransitionFromOpenToHalfOpenEnabled();

    default boolean parseAutomaticTransitionFromOpenToHalfOpenEnabled() {
        return parseStringToValue(automaticTransitionFromOpenToHalfOpenEnabled(),
                false, TO_BOOLEAN_FUNCTION);
    }

    @Config("waitDurationInOpenState")
    String waitDurationInOpenState();

    default Duration parseWaitDurationInOpenState() {
        return parseStringToValue(waitDurationInOpenState(),
                Duration.ofSeconds(DEFAULT_WAIT_DURATION_IN_OPEN_STATE),
                TO_DURATION_FUNCTION.andThen(Duration::ofMillis));
    }

    @Config("permittedNumberOfCallsInHalfOpenState")
    String permittedNumberOfCallsInHalfOpenState();

    default int parsePermittedNumberOfCallsInHalfOpenState() {
        return parseStringToValue(permittedNumberOfCallsInHalfOpenState(),
                DEFAULT_PERMITTED_CALLS_IN_HALF_OPEN_STATE, TO_INT_FUNCTION);
    }

    @Config("maxWaitDurationInHalfOpenState")
    String maxWaitDurationInHalfOpenState();

    default Duration parseMaxWaitDurationInHalfOpenState() {
        return parseStringToValue(maxWaitDurationInHalfOpenState(),
                Duration.ofSeconds(DEFAULT_WAIT_DURATION_IN_HALF_OPEN_STATE),
                TO_DURATION_FUNCTION.andThen(Duration::ofMillis));
    }

    @Override
    default CircuitBreaker circuitBreaker() {
        val circuitBreakerName = circuitBreakerName();
        if (isBlank(circuitBreakerName)) return null;
        return CircuitBreaker.of(circuitBreakerName, CircuitBreakerConfig.custom()
                .slidingWindow(parseSlidingWindowSize(),
                        parseMinimumNumberOfCalls(), parseSlidingWindowType())
                .failureRateThreshold(parseFailureRateThreshold())
                .slowCallRateThreshold(parseSlowCallRateThreshold())
                .slowCallDurationThreshold(parseSlowCallDurationThreshold())
                .automaticTransitionFromOpenToHalfOpenEnabled(parseAutomaticTransitionFromOpenToHalfOpenEnabled())
                .waitDurationInOpenState(parseWaitDurationInOpenState())
                .permittedNumberOfCallsInHalfOpenState(parsePermittedNumberOfCallsInHalfOpenState())
                .maxWaitDurationInHalfOpenState(parseMaxWaitDurationInHalfOpenState()).build());
    }
}
