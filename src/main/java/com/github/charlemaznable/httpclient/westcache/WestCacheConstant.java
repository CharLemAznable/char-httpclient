package com.github.charlemaznable.httpclient.westcache;

import com.github.charlemaznable.core.lang.ClzPath;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class WestCacheConstant {

    public static final boolean HAS_WESTCACHE =
            ClzPath.classExists("com.github.bingoohuang.westcache.utils.WestCacheOption");
}
