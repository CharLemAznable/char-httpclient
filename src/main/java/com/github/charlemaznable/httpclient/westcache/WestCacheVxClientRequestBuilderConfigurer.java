package com.github.charlemaznable.httpclient.westcache;

import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.github.charlemaznable.httpclient.common.CommonExecute;
import com.github.charlemaznable.httpclient.ohclient.elf.RequestBuilderConfigurer;
import com.google.auto.service.AutoService;
import lombok.val;
import okhttp3.Request;

import static com.github.charlemaznable.httpclient.westcache.WestCacheConstant.HAS_WESTCACHE;
import static java.util.Objects.nonNull;

@AutoService(RequestBuilderConfigurer.class)
public final class WestCacheVxClientRequestBuilderConfigurer implements RequestBuilderConfigurer {

    @Override
    public void configRequestBuilder(Request.Builder requestBuilder,
                                     CommonExecute<?, ?, ?> execute) {
        // westcache supported
        if (HAS_WESTCACHE) {
            val method = execute.executeMethod().method();
            val option = WestCacheOption.parseWestCacheable(method);
            if (nonNull(option)) {
                val cacheKey = option.getKeyer().getCacheKey(option,
                        method, execute.executeMethod().defaultClass(), execute.args());
                requestBuilder.tag(WestCacheContext.class, new WestCacheContext(option, cacheKey));
            }
        }
    }
}
