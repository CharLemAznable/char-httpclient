package com.github.charlemaznable.httpclient.westcache;

import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.github.charlemaznable.httpclient.common.CommonExecute;
import com.github.charlemaznable.httpclient.wfclient.elf.RequestSpecConfigurer;
import com.google.auto.service.AutoService;
import lombok.val;
import org.springframework.web.reactive.function.client.WebClient;

import static com.github.charlemaznable.httpclient.westcache.WestCacheConstant.HAS_WESTCACHE;
import static java.util.Objects.nonNull;

@AutoService(RequestSpecConfigurer.class)
public final class WestCacheWfClientRequestSpecConfigurer implements RequestSpecConfigurer {

    @Override
    public void configRequestSpec(WebClient.RequestBodyUriSpec requestSpec,
                                  CommonExecute<?, ?, ?, ?> execute) {
        // westcache supported
        if (HAS_WESTCACHE) {
            val method = execute.executeMethod().method();
            val option = WestCacheOption.parseWestCacheable(method);
            if (nonNull(option)) {
                val cacheKey = option.getKeyer().getCacheKey(option,
                        method, execute.executeMethod().defaultClass(), execute.args());
                requestSpec.attribute(WestCacheContext.class.getName(), new WestCacheContext(option, cacheKey));
            }
        }
    }
}
