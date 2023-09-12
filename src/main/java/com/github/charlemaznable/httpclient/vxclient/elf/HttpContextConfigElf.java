package com.github.charlemaznable.httpclient.vxclient.elf;

import com.github.charlemaznable.httpclient.common.CommonExecute;
import io.vertx.ext.web.client.impl.HttpContext;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class HttpContextConfigElf {

    private static final CopyOnWriteArrayList<HttpContextConfigurer> configurers;

    static {
        configurers = StreamSupport
                .stream(ServiceLoader.load(HttpContextConfigurer.class).spliterator(), false)
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    public static void configHttpContext(HttpContext<?> httpContext,
                                         CommonExecute<?, ?, ?, ?> execute) {
        for (val configurer : configurers) {
            configurer.configHttpContext(httpContext, execute);
        }
    }
}
