package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.core.lang.Objectt;
import com.github.charlemaznable.httpclient.common.ResilienceRateLimiterRecover;
import com.github.charlemaznable.httpclient.configurer.ResilienceRateLimiterConfigurer;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.apache.commons.lang3.BooleanUtils;

import java.time.Duration;

import static com.github.charlemaznable.configservice.impl.Functions.TO_DURATION_FUNCTION;
import static com.github.charlemaznable.configservice.impl.Functions.TO_INT_FUNCTION;
import static com.github.charlemaznable.configservice.impl.Functions.TO_LONG_FUNCTION;
import static com.github.charlemaznable.core.lang.Condition.checkNotBlank;
import static com.github.charlemaznable.core.lang.Condition.nonBlank;
import static com.github.charlemaznable.httpclient.configurer.configservice.ConfigurerElf.parseStringToValue;

public interface ResilienceRateLimiterConfig extends ResilienceRateLimiterConfigurer {

    @Config("enabledRateLimiter")
    String enabledRateLimiterString();

    default boolean enabledRateLimiter() {
        return BooleanUtils.toBoolean(enabledRateLimiterString());
    }

    @Config("rateLimiterName")
    String rateLimiterName();

    @Config("limitForPeriod")
    String limitForPeriod();

    default int parseLimitForPeriod() {
        return parseStringToValue(limitForPeriod(), 50, TO_INT_FUNCTION);
    }

    @Config("limitRefreshPeriodInNanos")
    String limitRefreshPeriodInNanos();

    default Duration parseLimitRefreshPeriod() {
        return parseStringToValue(limitRefreshPeriodInNanos(),
                Duration.ofNanos(500), TO_LONG_FUNCTION.andThen(Duration::ofNanos));
    }

    @Config("timeoutDuration")
    String timeoutDuration();

    default Duration parseTimeoutDuration() {
        return parseStringToValue(timeoutDuration(),
                Duration.ofMillis(5000), TO_DURATION_FUNCTION.andThen(Duration::ofMillis));
    }

    @Override
    default RateLimiter rateLimiter(String defaultName) {
        if (!enabledRateLimiter()) return null;
        return RateLimiter.of(
                checkNotBlank(nonBlank(rateLimiterName(), defaultName)),
                RateLimiterConfig.custom()
                        .limitForPeriod(parseLimitForPeriod())
                        .limitRefreshPeriod(parseLimitRefreshPeriod())
                        .timeoutDuration(parseTimeoutDuration()).build());
    }

    @Config("rateLimiterRecover")
    String rateLimiterRecoverString();

    @Override
    default ResilienceRateLimiterRecover<?> rateLimiterRecover() {
        return Objectt.parseObject(rateLimiterRecoverString(), ResilienceRateLimiterRecover.class);
    }
}
