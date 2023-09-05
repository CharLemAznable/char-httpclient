package com.github.charlemaznable.httpclient.vxclient.elf;

import com.github.charlemaznable.httpclient.common.CommonExecute;
import io.vertx.ext.web.client.impl.HttpContext;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.ServiceLoader;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class HttpContextConfigElf {

    private static final ServiceLoader<HttpContextConfigurer> configurers;

    static {
        configurers = ServiceLoader.load(HttpContextConfigurer.class);
    }

    public static void configHttpContext(HttpContext<?> httpContext,
                                         CommonExecute<?, ?, ?, ?> execute) {
        for (val configurer : configurers) {
            configurer.configHttpContext(httpContext, execute);
        }
    }
}
