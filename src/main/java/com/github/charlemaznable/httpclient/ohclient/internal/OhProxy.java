package com.github.charlemaznable.httpclient.ohclient.internal;

import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.httpclient.common.DefaultFallbackDisabled;
import com.github.charlemaznable.httpclient.common.FallbackFunction;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.MappingBalance;
import com.github.charlemaznable.httpclient.common.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.common.StatusErrorThrower;
import com.github.charlemaznable.httpclient.configurer.Configurer;
import com.github.charlemaznable.httpclient.configurer.DefaultFallbackDisabledConfigurer;
import com.github.charlemaznable.httpclient.configurer.MappingMethodNameDisabledConfigurer;
import com.github.charlemaznable.httpclient.ohclient.OhClient;
import com.github.charlemaznable.httpclient.ohclient.OhException;
import com.google.common.cache.LoadingCache;
import lombok.NoArgsConstructor;
import lombok.val;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static com.github.charlemaznable.core.lang.Condition.emptyThen;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.LoadingCachee.get;
import static com.github.charlemaznable.core.lang.LoadingCachee.simpleCache;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Mapp.of;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_ACCEPT_CHARSET;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_CONTENT_FORMATTER;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_HTTP_METHOD;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_LOGGING_LEVEL;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhDummy.ohConnectionPool;
import static com.google.common.cache.CacheLoader.from;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.core.annotation.AnnotatedElementUtils.isAnnotated;

@SuppressWarnings("rawtypes")
public final class OhProxy extends OhRoot implements MethodInterceptor, Reloadable {

    Class ohClass;
    Factory factory;
    Configurer configurer;
    List<String> baseUrls;
    boolean mappingMethodNameDisabled;

    LoadingCache<Method, OhMappingProxy> ohMappingProxyCache
            = simpleCache(from(this::loadMappingProxy));

    public OhProxy(Class ohClass, Factory factory) {
        this.ohClass = ohClass;
        this.factory = factory;
        this.initialize();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args,
                            MethodProxy methodProxy) throws Throwable {
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
        this.configurer = checkConfigurer(this.ohClass, this.factory);

        this.baseUrls = emptyThen(checkMappingUrls(this.configurer, this.ohClass), () -> newArrayList(""));
        this.mappingMethodNameDisabled = Elf.checkMappingMethodNameDisabled(this.configurer, this.ohClass);

        this.clientProxy = checkClientProxy(this.configurer, this.ohClass);
        this.sslRoot = Elf.checkClientSSL(this.configurer, this.ohClass, this.factory);
        this.connectionPool = nullThen(checkConnectionPool(
                this.configurer, this.ohClass), () -> ohConnectionPool);
        this.timeoutRoot = Elf.checkClientTimeout(this.configurer, this.ohClass);
        this.interceptors = checkClientInterceptors(this.configurer, this.ohClass, this.factory);
        this.loggingLevel = nullThen(checkClientLoggingLevel(
                this.configurer, this.ohClass), () -> DEFAULT_LOGGING_LEVEL);

        this.okHttpClient = buildOkHttpClient(this);

        this.acceptCharset = nullThen(checkAcceptCharset(
                this.configurer, this.ohClass), () -> DEFAULT_ACCEPT_CHARSET);
        this.contentFormatter = nullThen(checkContentFormatter(
                this.configurer, this.ohClass, this.factory), () -> DEFAULT_CONTENT_FORMATTER);
        this.httpMethod = nullThen(checkHttpMethod(
                this.configurer, this.ohClass), () -> DEFAULT_HTTP_METHOD);
        this.headers = checkFixedHeaders(this.configurer, this.ohClass);
        this.pathVars = checkFixedPathVars(this.configurer, this.ohClass);
        this.parameters = checkFixedParameters(this.configurer, this.ohClass);
        this.contexts = checkFixedContexts(this.configurer, this.ohClass);

        this.statusFallbackMapping = checkStatusFallbackMapping(this.configurer, this.ohClass);
        this.statusSeriesFallbackMapping = Elf.defaultFallback(this.configurer, this.ohClass);
        this.statusSeriesFallbackMapping.putAll(checkStatusSeriesFallbackMapping(this.configurer, this.ohClass));

        this.requestExtender = checkRequestExtender(this.configurer, this.ohClass, this.factory);
        this.responseParser = checkResponseParser(this.configurer, this.ohClass, this.factory);
        this.extraUrlQueryBuilder = checkExtraUrlQueryBuilder(this.configurer, this.ohClass, this.factory);
        this.mappingBalancer = nullThen(checkMappingBalancer(
                this.configurer, this.ohClass, this.factory), MappingBalance.RandomBalancer::new);
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

        static boolean checkMappingMethodNameDisabled(Configurer configurer, Class clazz) {
            if (configurer instanceof MappingMethodNameDisabledConfigurer)
                return ((MappingMethodNameDisabledConfigurer) configurer).disabledMappingMethodName();
            return isAnnotated(clazz, MappingMethodNameDisabled.class);
        }

        static SSLRoot checkClientSSL(Configurer configurer, Class clazz, Factory factory) {
            return OhRoot.checkClientSSL(configurer, clazz, factory, false, false, false, new SSLRoot());
        }

        static TimeoutRoot checkClientTimeout(Configurer configurer, Class clazz) {
            return OhRoot.checkClientTimeout(configurer, clazz, new TimeoutRoot());
        }

        static Map<HttpStatus.Series, Class<? extends FallbackFunction>> defaultFallback(Configurer configurer, Class clazz) {
            val disabled = configurer instanceof DefaultFallbackDisabledConfigurer
                    ? ((DefaultFallbackDisabledConfigurer) configurer).disabledDefaultFallback()
                    : isAnnotated(clazz, DefaultFallbackDisabled.class);
            return disabled ? newHashMap() : of(
                    HttpStatus.Series.CLIENT_ERROR, StatusErrorThrower.class,
                    HttpStatus.Series.SERVER_ERROR, StatusErrorThrower.class);
        }
    }
}
