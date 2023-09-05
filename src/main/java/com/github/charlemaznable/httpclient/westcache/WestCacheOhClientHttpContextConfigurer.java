package com.github.charlemaznable.httpclient.westcache;

import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.github.charlemaznable.httpclient.common.CommonExecute;
import com.github.charlemaznable.httpclient.vxclient.elf.HttpContextConfigurer;
import com.google.auto.service.AutoService;
import io.vertx.ext.web.client.impl.HttpContext;
import lombok.val;

import static com.github.charlemaznable.httpclient.westcache.WestCacheConstant.HAS_WESTCACHE;
import static java.util.Objects.nonNull;

@AutoService(HttpContextConfigurer.class)
public final class WestCacheOhClientHttpContextConfigurer implements HttpContextConfigurer {

    @Override
    public void configHttpContext(HttpContext<?> httpContext,
                                  CommonExecute<?, ?, ?, ?> execute) {
        // westcache supported
        if (HAS_WESTCACHE) {
            val method = execute.executeMethod().method();
            val option = WestCacheOption.parseWestCacheable(method);
            if (nonNull(option)) {
                val cacheKey = option.getKeyer().getCacheKey(option,
                        method, execute.executeMethod().defaultClass(), execute.args());
                httpContext.set(WestCacheContext.class.getName(), new WestCacheContext(option, cacheKey));
            }
        }
    }
}
