package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.BuddyEnhancer;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.httpclient.resilience.common.ResilienceMeterBinder;
import com.github.charlemaznable.httpclient.vxclient.enhancer.VxClientEnhancer;
import com.github.charlemaznable.httpclient.vxclient.internal.VxClass;
import com.github.charlemaznable.httpclient.vxclient.internal.VxDummy;
import com.google.common.cache.LoadingCache;
import io.vertx.core.Vertx;
import lombok.AllArgsConstructor;
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
import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class VxFactory {

    private static final LoadingCache<Factory, VxLoader>
            vxLoaderCache = simpleCache(from(VxLoader::new));
    private static final CopyOnWriteArrayList<VxClientEnhancer> enhancers;

    static {
        enhancers = StreamSupport
                .stream(ServiceLoader.load(VxClientEnhancer.class).spliterator(), false)
                .sorted(Comparator.comparingInt(VxClientEnhancer::getOrder).reversed())
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    public static <T> T getClient(Class<T> vxClass) {
        return vxLoader(FactoryContext.get()).getClient(vxClass);
    }

    public static VxLoader springVxLoader() {
        return vxLoader(springFactory());
    }

    public static VxLoader vxLoader(Factory factory) {
        return get(vxLoaderCache, factory);
    }

    public static class VxLoader {

        private final Factory factory;
        private final LoadingCache<Class<?>, Object> vxCache
                = simpleCache(from(this::loadClient));
        private final Object vertxLock = new Object();
        private volatile VertxHolder vertxHolder;

        VxLoader(Factory factory) {
            this.factory = checkNotNull(factory);
        }

        @SuppressWarnings("unchecked")
        public <T> T getClient(Class<T> vxClass) {
            return (T) get(vxCache, vxClass);
        }

        @Nonnull
        private <T> Object loadClient(@Nonnull Class<T> vxClass) {
            ensureClassIsAnInterface(vxClass);
            return wrapWithEnhancer(vxClass,
                    BuddyEnhancer.create(VxDummy.class,
                            new Object[]{vxClass},
                            new Class[]{vxClass, Reloadable.class, ResilienceMeterBinder.class},
                            invocation -> {
                                if (invocation.getMethod().isDefault() ||
                                        invocation.getMethod().getDeclaringClass()
                                                .equals(VxDummy.class)) return 1;
                                return 0;
                            },
                            new BuddyEnhancer.Delegate[]{
                                    new VxClass(vertx(), factory, vxClass),
                                    BuddyEnhancer.CALL_SUPER
                            }));
        }

        private Vertx vertx() {
            if (nonNull(vertxHolder)) return vertxHolder.vertx;
            synchronized (vertxLock) {
                if (nonNull(vertxHolder)) return vertxHolder.vertx;
                vertxHolder = new VertxHolder(checkNotNull(FactoryContext.build(factory, Vertx.class),
                        new VxException("Cannot find Vertx Instance in Context")));
                return vertxHolder.vertx;
            }
        }

        private <T> void ensureClassIsAnInterface(Class<T> clazz) {
            if (clazz.isInterface()) return;
            throw new VxException(clazz + " is not An Interface");
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

    @AllArgsConstructor
    private static final class VertxHolder {

        private final Vertx vertx;
    }
}
