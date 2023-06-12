package com.github.charlemaznable.httpclient.wfclient;

import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.BuddyEnhancer;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.httpclient.wfclient.enhancer.WfClientEnhancer;
import com.github.charlemaznable.httpclient.wfclient.internal.WfClass;
import com.github.charlemaznable.httpclient.wfclient.internal.WfDummy;
import com.google.common.cache.LoadingCache;
import lombok.NoArgsConstructor;
import lombok.val;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.LoadingCachee.get;
import static com.github.charlemaznable.core.lang.LoadingCachee.simpleCache;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;
import static com.google.common.cache.CacheLoader.from;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class WfFactory {

    private static final LoadingCache<Factory, WfLoader>
            wfLoaderCache = simpleCache(from(WfLoader::new));
    private static final List<WfClientEnhancer> enhancers;

    static {
        enhancers = StreamSupport
                .stream(ServiceLoader.load(WfClientEnhancer.class).spliterator(), false)
                .sorted(Comparator.comparingInt(WfClientEnhancer::getOrder).reversed())
                .collect(Collectors.toList());
    }

    public static <T> T getClient(Class<T> wfClass) {
        return wfLoader(FactoryContext.get()).getClient(wfClass);
    }

    public static WfLoader springWfLoader() {
        return wfLoader(springFactory());
    }

    public static WfLoader wfLoader(Factory factory) {
        return get(wfLoaderCache, factory);
    }

    public static class WfLoader {

        private final Factory factory;
        private final LoadingCache<Class<?>, Object> wfCache
                = simpleCache(from(this::loadClient));

        WfLoader(Factory factory) {
            this.factory = checkNotNull(factory);
        }

        @SuppressWarnings("unchecked")
        public <T> T getClient(Class<T> wfClass) {
            return (T) get(wfCache, wfClass);
        }

        @Nonnull
        private <T> Object loadClient(@Nonnull Class<T> wfClass) {
            ensureClassIsAnInterface(wfClass);
            return wrapWithEnhancer(wfClass,
                    BuddyEnhancer.create(WfDummy.class,
                            new Object[]{wfClass},
                            new Class[]{wfClass, Reloadable.class},
                            invocation -> {
                                if (invocation.getMethod().isDefault() ||
                                        invocation.getMethod().getDeclaringClass()
                                                .equals(WfDummy.class)) return 1;
                                return 0;
                            },
                            new BuddyEnhancer.Delegate[]{
                                    new WfClass(factory, wfClass),
                                    BuddyEnhancer.CALL_SUPER
                            }));
        }

        private <T> void ensureClassIsAnInterface(Class<T> clazz) {
            if (clazz.isInterface()) return;
            throw new WfException(clazz + " is not An Interface");
        }

        private <T> Object wrapWithEnhancer(Class<T> wfClass, Object impl) {
            Object enhancedImpl = impl;
            for (val enhancer : enhancers) {
                if (enhancer.isEnabled(wfClass)) {
                    enhancedImpl = enhancer.build(wfClass, enhancedImpl);
                }
            }
            return enhancedImpl;
        }
    }
}
