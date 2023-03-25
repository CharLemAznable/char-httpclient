package com.github.charlemaznable.httpclient.ohclient.internal;

import com.github.charlemaznable.configservice.ConfigListener;
import com.github.charlemaznable.core.lang.BuddyEnhancer;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.httpclient.configurer.Configurer;
import com.github.charlemaznable.httpclient.ohclient.OhClient;
import com.github.charlemaznable.httpclient.ohclient.OhException;
import com.google.common.cache.LoadingCache;
import lombok.NoArgsConstructor;
import lombok.val;

import java.lang.reflect.Method;
import java.util.List;

import static com.github.charlemaznable.core.lang.Condition.emptyThen;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.LoadingCachee.get;
import static com.github.charlemaznable.core.lang.LoadingCachee.simpleCache;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_LOGGING_LEVEL;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhDummy.ohConnectionPool;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhDummy.ohDispatcher;
import static com.google.common.cache.CacheLoader.from;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.core.annotation.AnnotatedElementUtils.isAnnotated;

@SuppressWarnings("rawtypes")
public final class OhProxy extends OhRoot implements BuddyEnhancer.Delegate, Reloadable {

    Class ohClass;
    Factory factory;
    ConfigListener configListener;
    Configurer configurer;
    List<String> baseUrls;
    boolean mappingMethodNameDisabled;

    LoadingCache<Method, OhMappingProxy> ohMappingProxyCache
            = simpleCache(from(this::loadMappingProxy));

    public OhProxy(Class ohClass, Factory factory) {
        this.ohClass = ohClass;
        this.factory = factory;
        this.configListener = (keyset, key, value) -> reload();
        this.initialize();
    }

    @Override
    public Object invoke(BuddyEnhancer.Invocation invocation) throws Exception {
        val method = invocation.getMethod();
        val args = invocation.getArguments();
        if (method.getDeclaringClass().equals(Reloadable.class)) {
            return method.invoke(this, args);
        }

        val mappingProxy = get(this.ohMappingProxyCache, method);
        return mappingProxy.execute(args);
    }

    @Override
    public void reload() {
        this.initialize();
        this.ohMappingProxyCache.invalidateAll();
    }

    private void initialize() {
        Elf.checkOhClient(this.ohClass);
        checkConfigurerIsRegisterThenRun(this.configurer, register ->
                register.removeConfigListener(this.configListener));
        this.configurer = checkConfigurer(this.ohClass, this.factory);
        checkConfigurerIsRegisterThenRun(this.configurer, register ->
                register.addConfigListener(this.configListener));

        setUpBeforeInitialization(this.configurer, this.ohClass);

        this.baseUrls = emptyThen(checkMappingUrls(
                this.configurer, this.ohClass, OhDummy::substitute), () -> newArrayList(""));
        this.mappingMethodNameDisabled = checkMappingMethodNameDisabled(this.configurer, this.ohClass);

        this.clientProxy = checkClientProxy(this.configurer, this.ohClass);
        this.sslRoot = Elf.checkClientSSL(this.configurer, this.ohClass, this.factory);
        this.dispatcher = nullThen(checkDispatcher(
                this.configurer, this.ohClass), () -> ohDispatcher);
        this.connectionPool = nullThen(checkConnectionPool(
                this.configurer, this.ohClass), () -> ohConnectionPool);
        this.timeoutRoot = Elf.checkClientTimeout(this.configurer, this.ohClass);
        this.interceptors = checkClientInterceptors(this.configurer, this.ohClass, this.factory);
        this.loggingLevel = nullThen(checkClientLoggingLevel(
                this.configurer, this.ohClass), () -> DEFAULT_LOGGING_LEVEL);
        this.okHttpClient = buildOkHttpClient(this);

        initialize(this, this.factory, this.configurer, this.ohClass);

        tearDownAfterInitialization(this.configurer, this.ohClass);
    }

    private OhMappingProxy loadMappingProxy(Method method) {
        return new OhMappingProxy(this.ohClass, method, this.factory, this);
    }

    @NoArgsConstructor(access = PRIVATE)
    static class Elf {

        static void checkOhClient(Class clazz) {
            if (isAnnotated(clazz, OhClient.class)) return;
            throw new OhException(clazz.getName() + " has no OhClient annotation");
        }

        static SSLRoot checkClientSSL(Configurer configurer, Class clazz, Factory factory) {
            return OhRoot.checkClientSSL(configurer, clazz, factory, false, false, false, new SSLRoot());
        }

        static TimeoutRoot checkClientTimeout(Configurer configurer, Class clazz) {
            return OhRoot.checkClientTimeout(configurer, clazz, new TimeoutRoot());
        }
    }
}
