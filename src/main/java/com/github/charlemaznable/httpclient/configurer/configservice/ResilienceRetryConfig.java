package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.configurer.ResilienceRetryConfigurer;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.apache.commons.lang3.BooleanUtils;

import java.time.Duration;

import static com.github.charlemaznable.configservice.impl.Functions.TO_DURATION_FUNCTION;
import static com.github.charlemaznable.configservice.impl.Functions.TO_INT_FUNCTION;
import static com.github.charlemaznable.core.lang.Condition.checkNotBlank;
import static com.github.charlemaznable.core.lang.Condition.nonBlank;
import static com.github.charlemaznable.httpclient.configurer.configservice.ConfigurerElf.parseStringToValue;
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

    @Override
    default Retry retry(String defaultName) {
        if (!enabledRetry()) return null;
        return Retry.of(
                checkNotBlank(nonBlank(retryName(), defaultName)),
                RetryConfig.custom()
                        .maxAttempts(parseMaxAttempts())
                        .waitDuration(parseWaitDuration()).build());
    }

    @Config("isolatedExecutor")
    String isolatedExecutorString();

    @Override
    default boolean isolatedExecutor() {
        return BooleanUtils.toBoolean(isolatedExecutorString());
    }
}
