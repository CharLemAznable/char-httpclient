package com.github.charlemaznable.httpclient.resilience.common;

import io.micrometer.core.instrument.MeterRegistry;

public interface ResilienceMeterBinder {

    void resilienceBindTo(MeterRegistry registry);
}
