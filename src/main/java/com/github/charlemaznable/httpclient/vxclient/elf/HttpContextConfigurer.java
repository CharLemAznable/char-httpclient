package com.github.charlemaznable.httpclient.vxclient.elf;

import com.github.charlemaznable.httpclient.common.CommonExecute;
import io.vertx.ext.web.client.impl.HttpContext;

public interface HttpContextConfigurer {

    void configHttpContext(HttpContext<?> httpContext,
                           CommonExecute<?, ?, ?, ?> execute);
}
