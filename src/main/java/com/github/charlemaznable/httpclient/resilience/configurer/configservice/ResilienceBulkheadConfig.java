package com.github.charlemaznable.httpclient.resilience.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.core.lang.Objectt;
import com.github.charlemaznable.httpclient.resilience.configurer.ResilienceBulkheadConfigurer;
import com.github.charlemaznable.httpclient.resilience.function.ResilienceBulkheadRecover;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import org.apache.commons.lang3.BooleanUtils;

import java.time.Duration;

import static com.github.charlemaznable.configservice.impl.Functions.TO_DURATION_FUNCTION;
import static com.github.charlemaznable.configservice.impl.Functions.TO_INT_FUNCTION;
import static com.github.charlemaznable.configservice.impl.Functions.parseStringToValue;
import static com.github.charlemaznable.core.lang.Condition.checkNotBlank;
import static com.github.charlemaznable.core.lang.Condition.nonBlank;
import static io.github.resilience4j.bulkhead.BulkheadConfig.DEFAULT_MAX_CONCURRENT_CALLS;
import static io.github.resilience4j.bulkhead.BulkheadConfig.DEFAULT_MAX_WAIT_DURATION;

public interface ResilienceBulkheadConfig extends ResilienceBulkheadConfigurer {

    @Config("enabledBulkhead")
    String enabledBulkheadString();

    default boolean enabledBulkhead() {
        return BooleanUtils.toBoolean(enabledBulkheadString());
    }

    @Config("bulkheadName")
    String bulkheadName();

    @Config("maxConcurrentCalls")
    String maxConcurrentCalls();

    default int parseMaxConcurrentCalls() {
        return parseStringToValue(maxConcurrentCalls(),
                DEFAULT_MAX_CONCURRENT_CALLS, TO_INT_FUNCTION);
    }

    @Config("maxWaitDuration")
    String maxWaitDuration();

    default Duration parseMaxWaitDuration() {
        return parseStringToValue(maxWaitDuration(),
                DEFAULT_MAX_WAIT_DURATION, TO_DURATION_FUNCTION.andThen(Duration::ofMillis));
    }

    @Override
    default Bulkhead bulkhead(String defaultName) {
        if (!enabledBulkhead()) return null;
        return Bulkhead.of(
                checkNotBlank(nonBlank(bulkheadName(), defaultName)),
                BulkheadConfig.custom()
                        .maxConcurrentCalls(parseMaxConcurrentCalls())
                        .maxWaitDuration(parseMaxWaitDuration()).build());
    }

    @Config("bulkheadRecover")
    String bulkheadRecoverString();

    @SuppressWarnings("unchecked")
    @Override
    default <T> ResilienceBulkheadRecover<T> bulkheadRecover() {
        return Objectt.parseObject(bulkheadRecoverString(), ResilienceBulkheadRecover.class);
    }
}
