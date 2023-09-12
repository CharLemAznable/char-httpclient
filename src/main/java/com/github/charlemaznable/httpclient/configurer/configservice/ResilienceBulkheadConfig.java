package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.configurer.ResilienceBulkheadConfigurer;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import lombok.val;

import java.time.Duration;

import static com.github.charlemaznable.configservice.impl.Functions.TO_DURATION_FUNCTION;
import static com.github.charlemaznable.configservice.impl.Functions.TO_INT_FUNCTION;
import static com.github.charlemaznable.core.lang.Str.isBlank;
import static com.github.charlemaznable.httpclient.configurer.configservice.ConfigurerElf.parseStringToValue;
import static io.github.resilience4j.bulkhead.BulkheadConfig.DEFAULT_MAX_CONCURRENT_CALLS;
import static io.github.resilience4j.bulkhead.BulkheadConfig.DEFAULT_MAX_WAIT_DURATION;

public interface ResilienceBulkheadConfig extends ResilienceBulkheadConfigurer {

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
    default Bulkhead bulkhead() {
        val bulkheadName = bulkheadName();
        if (isBlank(bulkheadName)) return null;
        return Bulkhead.of(bulkheadName, BulkheadConfig.custom()
                .maxConcurrentCalls(parseMaxConcurrentCalls())
                .maxWaitDuration(parseMaxWaitDuration()).build());
    }
}
