package com.github.charlemaznable.httpclient.ohclient.enhancer;

import com.github.bingoohuang.westcache.cglib.CacheMethodInterceptor;
import com.github.bingoohuang.westcache.utils.Anns;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.github.charlemaznable.core.lang.ClzPath;
import com.github.charlemaznable.core.lang.EasyEnhancer;
import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.httpclient.ohclient.internal.OhDummy;
import com.google.auto.service.AutoService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.util.concurrent.Executors.newCachedThreadPool;

@AutoService(OhClientEnhancer.class)
public final class WestCacheableOhClientEnhancer implements OhClientEnhancer {

    static final ExecutorService cacheExecutorService = newCachedThreadPool();

    @Override
    public boolean isEnabled(Class<?> clientClass) {
        return ClzPath.classExists("com.github.bingoohuang.westcache.cglib.CacheMethodInterceptor")
                && Anns.isFastWestCacheAnnotated(clientClass);
    }

    @Override
    public Object build(Class<?> clientClass, Object clientImpl) {
        return EasyEnhancer.create(OhDummy.class,
                new Class[]{clientClass, Reloadable.class},
                new WestCacheableOhClientInterceptor(clientImpl),
                new Object[]{clientClass});
    }

    @AllArgsConstructor
    static final class WestCacheableOhClientInterceptor
            extends CacheMethodInterceptor<MethodProxy>
            implements MethodInterceptor {

        @Nonnull
        private Object target;

        @SneakyThrows
        @Override
        public Object intercept(Object obj, Method method,
                                Object[] arguments, MethodProxy methodProxy) {
            val option = WestCacheOption.parseWestCacheable(method);
            if (option == null) return methodProxy.invoke(target, arguments);

            if (Future.class == method.getReturnType())
                return cacheExecutorService.submit(() ->
                        super.intercept(obj, method, arguments, methodProxy));
            return super.intercept(obj, method, arguments, methodProxy);
        }

        @SneakyThrows
        @Override
        protected Object invokeRaw(Object obj, Object[] args, MethodProxy methodProxy) {
            val raw = methodProxy.invoke(target, args);
            return raw instanceof Future ? ((Future<?>) raw).get() : raw;
        }

        @Override
        protected String getCacheKey(WestCacheOption option,
                                     Object obj,
                                     Method method,
                                     Object[] args,
                                     MethodProxy methodProxy) {
            return option.getKeyer().getCacheKey(option, method, target, args);
        }
    }
}
