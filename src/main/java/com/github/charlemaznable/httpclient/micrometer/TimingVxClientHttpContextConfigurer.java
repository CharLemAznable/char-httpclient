package com.github.charlemaznable.httpclient.micrometer;

import com.github.charlemaznable.httpclient.common.CommonExecute;
import com.github.charlemaznable.httpclient.vxclient.elf.HttpContextConfigurer;
import com.google.auto.service.AutoService;
import io.micrometer.core.instrument.MeterRegistry;
import io.vertx.ext.web.client.impl.HttpContext;

@AutoService(HttpContextConfigurer.class)
public final class TimingVxClientHttpContextConfigurer implements HttpContextConfigurer {

    @Override
    public void configHttpContext(HttpContext<?> httpContext,
                                  CommonExecute<?, ?, ?, ?> execute) {
        httpContext.set(MeterRegistry.class.getName(), execute.base().meterRegistry());
    }
}
