package com.github.charlemaznable.httpclient.ohclient.enhancer;

import com.github.bingoohuang.westcache.cglib.CacheMethodInterceptor;
import com.github.bingoohuang.westcache.utils.Anns;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.github.charlemaznable.core.lang.BuddyEnhancer;
import com.github.charlemaznable.core.lang.ClzPath;
import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.httpclient.ohclient.internal.OhDummy;
import com.google.auto.service.AutoService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.lang.concurrent.Executors.parallelismExecutor;

@AutoService(OhClientEnhancer.class)
public final class WestCacheableOhClientEnhancer implements OhClientEnhancer {

    static final ExecutorService cacheExecutorService = parallelismExecutor(0, 8);

    @Override
    public boolean isEnabled(Class<?> clientClass) {
        return ClzPath.classExists("com.github.bingoohuang.westcache.cglib.CacheMethodInterceptor")
                && Anns.isFastWestCacheAnnotated(clientClass);
    }

    @Override
    public Object build(Class<?> clientClass, Object clientImpl) {
        return BuddyEnhancer.create(OhDummy.class,
                new Object[]{clientClass},
                new Class[]{clientClass, Reloadable.class},
                new WestCacheableOhClientInterceptor(clientImpl));
    }

    @AllArgsConstructor
    static final class WestCacheableOhClientInterceptor
            extends CacheMethodInterceptor<BuddyEnhancer.Invocation>
            implements BuddyEnhancer.Delegate {

        @Nonnull
        private Object target;

        @Override
        public Object invoke(BuddyEnhancer.Invocation invocation) throws Exception {
            val method = invocation.getMethod();
            val arguments = invocation.getArguments();
            val option = WestCacheOption.parseWestCacheable(method);
            if (option == null) return method.invoke(target, arguments);

            if (Future.class == method.getReturnType())
                return cacheExecutorService.submit(() ->
                        intercept(invocation.getThis(), method, arguments, invocation));
            return intercept(invocation.getThis(), method, arguments, invocation);
        }

        @SneakyThrows
        @Override
        protected Object invokeRaw(Object obj, Object[] args, BuddyEnhancer.Invocation invocation) {
            val raw = invocation.getMethod().invoke(target, invocation.getArguments());
            return raw instanceof Future futureRaw ? futureRaw.get() : raw;
        }

        @Override
        protected String getCacheKey(WestCacheOption option,
                                     Object obj,
                                     Method method,
                                     Object[] args,
                                     BuddyEnhancer.Invocation proxy) {
            return option.getKeyer().getCacheKey(option, method, target, args);
        }
    }
}
