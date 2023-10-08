package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.BuddyEnhancer;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.httpclient.ohclient.enhancer.OhClientEnhancer;
import com.github.charlemaznable.httpclient.ohclient.internal.OhClass;
import com.github.charlemaznable.httpclient.ohclient.internal.OhDummy;
import com.github.charlemaznable.httpclient.common.MeterBinder;
import com.google.common.cache.LoadingCache;
import lombok.NoArgsConstructor;
import lombok.val;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.LoadingCachee.get;
import static com.github.charlemaznable.core.lang.LoadingCachee.simpleCache;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;
import static com.google.common.cache.CacheLoader.from;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class OhFactory {

    private static final LoadingCache<Factory, OhLoader>
            ohLoaderCache = simpleCache(from(OhLoader::new));
    private static final CopyOnWriteArrayList<OhClientEnhancer> enhancers;

    static {
        enhancers = StreamSupport
                .stream(ServiceLoader.load(OhClientEnhancer.class).spliterator(), false)
                .sorted(Comparator.comparingInt(OhClientEnhancer::getOrder).reversed())
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    public static <T> T getClient(Class<T> ohClass) {
        return ohLoader(FactoryContext.get()).getClient(ohClass);
    }

    public static OhLoader springOhLoader() {
        return ohLoader(springFactory());
    }

    public static OhLoader ohLoader(Factory factory) {
        return get(ohLoaderCache, factory);
    }

    public static class OhLoader {

        private final Factory factory;
        private final LoadingCache<Class<?>, Object> ohCache
                = simpleCache(from(this::loadClient));

        OhLoader(Factory factory) {
            this.factory = checkNotNull(factory);
        }

        @SuppressWarnings("unchecked")
        public <T> T getClient(Class<T> ohClass) {
            return (T) get(ohCache, ohClass);
        }

        @Nonnull
        private <T> Object loadClient(@Nonnull Class<T> ohClass) {
            ensureClassIsAnInterface(ohClass);
            return wrapWithEnhancer(ohClass,
                    BuddyEnhancer.create(OhDummy.class,
                            new Object[]{ohClass},
                            new Class[]{ohClass, Reloadable.class, MeterBinder.class},
                            invocation -> {
                                if (invocation.getMethod().isDefault() ||
                                        invocation.getMethod().getDeclaringClass()
                                                .equals(OhDummy.class)) return 1;
                                return 0;
                            },
                            new BuddyEnhancer.Delegate[]{
                                    new OhClass(factory, ohClass),
                                    BuddyEnhancer.CALL_SUPER
                            }));
        }

        private <T> void ensureClassIsAnInterface(Class<T> clazz) {
            if (clazz.isInterface()) return;
            throw new OhException(clazz + " is not An Interface");
        }

        private <T> Object wrapWithEnhancer(Class<T> ohClass, Object impl) {
            Object enhancedImpl = impl;
            for (val enhancer : enhancers) {
                if (enhancer.isEnabled(ohClass)) {
                    enhancedImpl = enhancer.build(ohClass, enhancedImpl);
                }
            }
            return enhancedImpl;
        }
    }
}
