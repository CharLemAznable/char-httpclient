package com.github.charlemaznable.httpclient.resilience.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.core.lang.Objectt;
import com.github.charlemaznable.httpclient.resilience.configurer.ResilienceTimeLimiterConfigurer;
import com.github.charlemaznable.httpclient.resilience.function.ResilienceTimeLimiterRecover;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.apache.commons.lang3.BooleanUtils;

import java.time.Duration;

import static com.github.charlemaznable.configservice.impl.Functions.TO_DURATION_FUNCTION;
import static com.github.charlemaznable.configservice.impl.Functions.parseStringToValue;
import static com.github.charlemaznable.core.lang.Condition.checkNotBlank;
import static com.github.charlemaznable.core.lang.Condition.nonBlank;

public interface ResilienceTimeLimiterConfig extends ResilienceTimeLimiterConfigurer {

    @Config("enabledTimeLimiter")
    String enabledTimeLimiterString();

    default boolean enabledTimeLimiter() {
        return BooleanUtils.toBoolean(enabledTimeLimiterString());
    }

    @Config("timeLimiterName")
    String timeLimiterName();

    @Config("timeoutDuration")
    String timeoutDuration();

    default Duration parseTimeoutDuration() {
        return parseStringToValue(timeoutDuration(),
                Duration.ofMillis(1000), TO_DURATION_FUNCTION.andThen(Duration::ofMillis));
    }

    @Override
    default TimeLimiter timeLimiter(String defaultName) {
        if (!enabledTimeLimiter()) return null;
        return TimeLimiter.of(
                checkNotBlank(nonBlank(timeLimiterName(), defaultName)),
                TimeLimiterConfig.custom()
                        .timeoutDuration(parseTimeoutDuration()).build());
    }

    @Config("timeLimiterRecover")
    String timeLimiterRecoverString();

    @SuppressWarnings("unchecked")
    @Override
    default <T> ResilienceTimeLimiterRecover<T> timeLimiterRecover() {
        return Objectt.parseObject(timeLimiterRecoverString(), ResilienceTimeLimiterRecover.class);
    }
}
