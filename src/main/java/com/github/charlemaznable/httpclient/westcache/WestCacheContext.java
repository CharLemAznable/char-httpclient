package com.github.charlemaznable.httpclient.westcache;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Optional;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.val;

@AllArgsConstructor
@EqualsAndHashCode
public final class WestCacheContext {

    private WestCacheOption option;
    private String cacheKey;

    public WestCacheItem cacheGet() {
        return option.getManager().get(option, cacheKey);
    }

    @SuppressWarnings("Guava")
    public <T> void cachePut(T value) {
        val item = new WestCacheItem(Optional.of(value), option);
        option.getManager().put(option, cacheKey, item);
    }
}
