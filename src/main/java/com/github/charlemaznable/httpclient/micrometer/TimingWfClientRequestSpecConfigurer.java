package com.github.charlemaznable.httpclient.micrometer;

import com.github.charlemaznable.httpclient.common.CommonExecute;
import com.github.charlemaznable.httpclient.wfclient.elf.RequestSpecConfigurer;
import com.google.auto.service.AutoService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.web.reactive.function.client.WebClient;

@AutoService(RequestSpecConfigurer.class)
public final class TimingWfClientRequestSpecConfigurer implements RequestSpecConfigurer {

    @Override
    public void configRequestSpec(WebClient.RequestBodyUriSpec requestSpec,
                                  CommonExecute<?, ?, ?, ?> execute) {
        requestSpec.attribute(MeterRegistry.class.getName(), execute.base().meterRegistry());
    }
}
