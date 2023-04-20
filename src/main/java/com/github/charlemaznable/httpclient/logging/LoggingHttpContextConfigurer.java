package com.github.charlemaznable.httpclient.logging;

import com.github.charlemaznable.httpclient.common.CommonExecute;
import com.github.charlemaznable.httpclient.vxclient.elf.HttpContextConfigurer;
import com.google.auto.service.AutoService;
import io.vertx.ext.web.client.impl.HttpContext;
import org.slf4j.Logger;

@AutoService(HttpContextConfigurer.class)
public final class LoggingHttpContextConfigurer implements HttpContextConfigurer {

    @Override
    public void configHttpContext(HttpContext<?> httpContext,
                                  CommonExecute<?, ?, ?> execute) {
        httpContext.set(Logger.class.getName(), execute.executeMethod().defaultClass().logger());
    }
}
