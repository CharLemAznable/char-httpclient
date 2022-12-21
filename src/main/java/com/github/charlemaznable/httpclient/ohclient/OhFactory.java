package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.BuddyEnhancer;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientTimeout;
import com.github.charlemaznable.httpclient.ohclient.enhancer.OhClientEnhancer;
import com.github.charlemaznable.httpclient.ohclient.internal.OhDummy;
import com.github.charlemaznable.httpclient.ohclient.internal.OhProxy;
import com.google.common.cache.LoadingCache;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.LoadingCachee.get;
import static com.github.charlemaznable.core.lang.LoadingCachee.simpleCache;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_CALL_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_CONNECT_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_READ_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_WRITE_TIMEOUT;
import static com.google.common.cache.CacheLoader.from;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class OhFactory {

    private static final LoadingCache<Factory, OhLoader>
            ohLoaderCache = simpleCache(from(OhLoader::new));
    private static final List<OhClientEnhancer> enhancers;

    static {
        enhancers = StreamSupport
                .stream(ServiceLoader.load(OhClientEnhancer.class).spliterator(), false)
                .sorted(Comparator.comparingInt(OhClientEnhancer::getOrder).reversed())
                .collect(Collectors.toList());
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

    public static ClientTimeout timeout() {
        return new ClientTimeoutImpl();
    }

    public static ClientTimeout timeout(
            long callTimeout, long connectTimeout,
            long readTimeout, long writeTimeout) {
        return new ClientTimeoutImpl(callTimeout,
                connectTimeout, readTimeout, writeTimeout);
    }

    @SuppressWarnings("unchecked")
    public static class OhLoader {

        private final Factory factory;
        private final LoadingCache<Class<?>, Object> ohCache
                = simpleCache(from(this::loadClient));

        OhLoader(Factory factory) {
            this.factory = checkNotNull(factory);
        }

        public <T> T getClient(Class<T> ohClass) {
            return (T) get(ohCache, ohClass);
        }

        @Nonnull
        private <T> Object loadClient(@Nonnull Class<T> ohClass) {
            ensureClassIsAnInterface(ohClass);
            return wrapWithEnhancer(ohClass,
                    BuddyEnhancer.create(OhDummy.class,
                            new Object[]{ohClass},
                            new Class[]{ohClass, Reloadable.class},
                            invocation -> {
                                if (invocation.getMethod().isDefault() ||
                                        invocation.getMethod().getDeclaringClass()
                                                .equals(OhDummy.class)) return 1;
                                return 0;
                            },
                            new BuddyEnhancer.Delegate[]{
                                    new OhProxy(ohClass, factory),
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

    @SuppressWarnings("ClassExplicitlyAnnotation")
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Accessors(fluent = true)
    private static class ClientTimeoutImpl implements ClientTimeout {

        private long callTimeout = DEFAULT_CALL_TIMEOUT; // in milliseconds
        private long connectTimeout = DEFAULT_CONNECT_TIMEOUT; // in milliseconds
        private long readTimeout = DEFAULT_READ_TIMEOUT; // in milliseconds
        private long writeTimeout = DEFAULT_WRITE_TIMEOUT; // in milliseconds

        @Override
        public Class<? extends TimeoutProvider> callTimeoutProvider() {
            return TimeoutProvider.class;
        }

        @Override
        public Class<? extends TimeoutProvider> connectTimeoutProvider() {
            return TimeoutProvider.class;
        }

        @Override
        public Class<? extends TimeoutProvider> readTimeoutProvider() {
            return TimeoutProvider.class;
        }

        @Override
        public Class<? extends TimeoutProvider> writeTimeoutProvider() {
            return TimeoutProvider.class;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return ClientTimeout.class;
        }
    }
}
