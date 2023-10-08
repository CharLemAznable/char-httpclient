package com.github.charlemaznable.httpclient.common;

import io.micrometer.core.instrument.MeterRegistry;

public interface MeterBinder {

    void bindTo(MeterRegistry registry);
}
