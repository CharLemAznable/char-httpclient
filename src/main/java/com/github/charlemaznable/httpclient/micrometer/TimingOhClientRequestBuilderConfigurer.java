package com.github.charlemaznable.httpclient.micrometer;

import com.github.charlemaznable.httpclient.common.CommonExecute;
import com.github.charlemaznable.httpclient.ohclient.elf.RequestBuilderConfigurer;
import com.google.auto.service.AutoService;
import io.micrometer.core.instrument.MeterRegistry;
import okhttp3.Request;

@AutoService(RequestBuilderConfigurer.class)
public final class TimingOhClientRequestBuilderConfigurer implements RequestBuilderConfigurer {

    @Override
    public void configRequestBuilder(Request.Builder requestBuilder,
                                     CommonExecute<?, ?, ?, ?> execute) {
        requestBuilder.tag(MeterRegistry.class, execute.base().meterRegistry());
    }
}
