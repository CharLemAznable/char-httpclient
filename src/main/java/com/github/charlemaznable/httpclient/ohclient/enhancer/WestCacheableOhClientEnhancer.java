package com.github.charlemaznable.httpclient.ohclient.enhancer;

import com.github.bingoohuang.westcache.cglib.CglibCacheMethodInterceptor;
import com.github.bingoohuang.westcache.utils.Anns;
import com.github.charlemaznable.core.lang.ClzPath;
import com.github.charlemaznable.core.lang.EasyEnhancer;
import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.httpclient.ohclient.internal.OhDummy;
import com.google.auto.service.AutoService;

@AutoService(OhClientEnhancer.class)
public final class WestCacheableOhClientEnhancer implements OhClientEnhancer {

    @Override
    public boolean isEnabled(Class<?> clientClass) {
        return ClzPath.classExists("org.springframework.cglib.proxy.Enhancer")
                && ClzPath.classExists("com.github.bingoohuang.westcache.cglib.CglibCacheMethodInterceptor")
                && Anns.isFastWestCacheAnnotated(clientClass);
    }

    @Override
    public Object build(Class<?> clientClass, Object clientImpl) {
        return EasyEnhancer.create(OhDummy.class,
                new Class[]{clientClass, Reloadable.class},
                new CglibCacheMethodInterceptor(clientImpl),
                new Object[]{clientClass});
    }
}
