package com.github.charlemaznable.httpclient.westcache;

import com.github.charlemaznable.core.lang.ClzPath;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class WestCacheConstant {

    public static final boolean HAS_WESTCACHE =
            ClzPath.classExists("com.github.bingoohuang.westcache.utils.WestCacheOption");

    public static Set<Integer> buildDefaultStatusCodes() {
        Set<Integer> codes = new HashSet<>(3);
        Collections.addAll(codes, 200, 301, 404);
        return codes;
    }
}
