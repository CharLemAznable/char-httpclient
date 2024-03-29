package com.github.charlemaznable.httpclient.resilience.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.core.lang.Objectt;
import com.github.charlemaznable.httpclient.resilience.configurer.ResilienceRetryConfigurer;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.apache.commons.lang3.BooleanUtils;

import java.time.Duration;
import java.util.function.Predicate;

import static com.github.charlemaznable.configservice.impl.Functions.TO_BOOLEAN_FUNCTION;
import static com.github.charlemaznable.configservice.impl.Functions.TO_DURATION_FUNCTION;
import static com.github.charlemaznable.configservice.impl.Functions.TO_INT_FUNCTION;
import static com.github.charlemaznable.configservice.impl.Functions.parseStringToValue;
import static com.github.charlemaznable.core.lang.Condition.checkNotBlank;
import static com.github.charlemaznable.core.lang.Condition.nonBlank;
import static com.github.charlemaznable.httpclient.resilience.common.ResilienceDefaults.checkResultPredicate;
import static io.github.resilience4j.retry.RetryConfig.DEFAULT_MAX_ATTEMPTS;

public interface ResilienceRetryConfig extends ResilienceRetryConfigurer {

    @Config("enabledRetry")
    String enabledRetryString();

    default boolean enabledRetry() {
        return BooleanUtils.toBoolean(enabledRetryString());
    }

    @Config("retryName")
    String retryName();

    @Config("maxAttempts")
    String maxAttempts();

    default int parseMaxAttempts() {
        return parseStringToValue(maxAttempts(),
                DEFAULT_MAX_ATTEMPTS, TO_INT_FUNCTION);
    }

    @Config("waitDuration")
    String waitDuration();

    default Duration parseWaitDuration() {
        return parseStringToValue(waitDuration(),
                Duration.ofMillis(500L), TO_DURATION_FUNCTION.andThen(Duration::ofMillis));
    }

    @Config("retryOnResultPredicate")
    String retryOnResultPredicate();

    default Predicate<Object> parseRetryOnResultPredicate() {
        return checkResultPredicate(Objectt.parseObject(retryOnResultPredicate(), Predicate.class));
    }

    @Config("failAfterMaxAttempts")
    String failAfterMaxAttempts();

    default boolean parseFailAfterMaxAttempts() {
        return parseStringToValue(failAfterMaxAttempts(), false, TO_BOOLEAN_FUNCTION);
    }

    @Override
    default Retry retry(String defaultName) {
        if (!enabledRetry()) return null;
        return Retry.of(
                checkNotBlank(nonBlank(retryName(), defaultName)),
                RetryConfig.custom()
                        .maxAttempts(parseMaxAttempts())
                        .waitDuration(parseWaitDuration())
                        .retryOnResult(checkResultPredicate(parseRetryOnResultPredicate()))
                        .failAfterMaxAttempts(parseFailAfterMaxAttempts()).build());
    }

    @Config("isolatedExecutor")
    String isolatedExecutorString();

    @Override
    default boolean isolatedExecutor() {
        return BooleanUtils.toBoolean(isolatedExecutorString());
    }
}
