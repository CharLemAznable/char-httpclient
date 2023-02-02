package com.github.charlemaznable.httpclient.ohclient.internal;

import com.github.charlemaznable.configservice.ConfigFactory;
import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.BuddyEnhancer;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.httpclient.common.AcceptCharset;
import com.github.charlemaznable.httpclient.common.ConfigureWith;
import com.github.charlemaznable.httpclient.common.ContentFormat;
import com.github.charlemaznable.httpclient.common.ContentFormat.ContentFormatter;
import com.github.charlemaznable.httpclient.common.DefaultFallbackDisabled;
import com.github.charlemaznable.httpclient.common.ExtraUrlQuery;
import com.github.charlemaznable.httpclient.common.ExtraUrlQuery.ExtraUrlQueryBuilder;
import com.github.charlemaznable.httpclient.common.FallbackFunction;
import com.github.charlemaznable.httpclient.common.FixedContext;
import com.github.charlemaznable.httpclient.common.FixedHeader;
import com.github.charlemaznable.httpclient.common.FixedParameter;
import com.github.charlemaznable.httpclient.common.FixedPathVar;
import com.github.charlemaznable.httpclient.common.FixedValueProvider;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.common.Mapping.UrlProvider;
import com.github.charlemaznable.httpclient.common.MappingBalance;
import com.github.charlemaznable.httpclient.common.MappingBalance.MappingBalancer;
import com.github.charlemaznable.httpclient.common.MappingBalance.RandomBalancer;
import com.github.charlemaznable.httpclient.common.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.common.RequestExtend;
import com.github.charlemaznable.httpclient.common.RequestExtend.RequestExtender;
import com.github.charlemaznable.httpclient.common.RequestMethod;
import com.github.charlemaznable.httpclient.common.ResponseParse;
import com.github.charlemaznable.httpclient.common.ResponseParse.ResponseParser;
import com.github.charlemaznable.httpclient.common.StatusErrorThrower;
import com.github.charlemaznable.httpclient.common.StatusFallback;
import com.github.charlemaznable.httpclient.common.StatusSeriesFallback;
import com.github.charlemaznable.httpclient.configurer.AcceptCharsetConfigurer;
import com.github.charlemaznable.httpclient.configurer.Configurer;
import com.github.charlemaznable.httpclient.configurer.ContentFormatConfigurer;
import com.github.charlemaznable.httpclient.configurer.DefaultFallbackDisabledConfigurer;
import com.github.charlemaznable.httpclient.configurer.ExtraUrlQueryConfigurer;
import com.github.charlemaznable.httpclient.configurer.FixedContextsConfigurer;
import com.github.charlemaznable.httpclient.configurer.FixedHeadersConfigurer;
import com.github.charlemaznable.httpclient.configurer.FixedParametersConfigurer;
import com.github.charlemaznable.httpclient.configurer.FixedPathVarsConfigurer;
import com.github.charlemaznable.httpclient.configurer.MappingBalanceConfigurer;
import com.github.charlemaznable.httpclient.configurer.MappingConfigurer;
import com.github.charlemaznable.httpclient.configurer.MappingMethodNameDisabledConfigurer;
import com.github.charlemaznable.httpclient.configurer.RequestExtendConfigurer;
import com.github.charlemaznable.httpclient.configurer.RequestMethodConfigurer;
import com.github.charlemaznable.httpclient.configurer.ResponseParseConfigurer;
import com.github.charlemaznable.httpclient.configurer.StatusFallbacksConfigurer;
import com.github.charlemaznable.httpclient.configurer.StatusSeriesFallbacksConfigurer;
import com.github.charlemaznable.httpclient.ohclient.OhClient;
import com.github.charlemaznable.httpclient.ohclient.OhException;
import com.github.charlemaznable.httpclient.ohclient.OhReq;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientInterceptor;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientInterceptor.InterceptorProvider;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientLoggingLevel;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientLoggingLevel.LoggingLevelProvider;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientProxy;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientProxy.ProxyProvider;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientSSL;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientSSL.HostnameVerifierProvider;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientSSL.SSLSocketFactoryProvider;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientSSL.X509TrustManagerProvider;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientTimeout;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientTimeout.TimeoutProvider;
import com.github.charlemaznable.httpclient.ohclient.annotation.IsolatedConnectionPool;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientInterceptorsConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientLoggingLevelConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientProxyConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientSSLConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientTimeoutConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.IsolatedConnectionPoolConfigurer;
import com.google.common.cache.LoadingCache;
import lombok.NoArgsConstructor;
import lombok.val;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import org.apache.commons.lang3.tuple.Pair;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.charlemaznable.core.lang.Condition.checkBlank;
import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.emptyThen;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.LoadingCachee.get;
import static com.github.charlemaznable.core.lang.LoadingCachee.simpleCache;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Mapp.of;
import static com.github.charlemaznable.core.lang.Mapp.toMap;
import static com.github.charlemaznable.core.lang.Str.isNotBlank;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_ACCEPT_CHARSET;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_CONTENT_FORMATTER;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_HTTP_METHOD;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_LOGGING_LEVEL;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhDummy.log;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhDummy.ohConnectionPool;
import static com.google.common.cache.CacheLoader.from;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedRepeatableAnnotations;
import static org.springframework.core.annotation.AnnotatedElementUtils.isAnnotated;

@SuppressWarnings("rawtypes")
public final class OhProxy extends OhRoot implements BuddyEnhancer.Delegate, Reloadable {

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
        this.configurer = Elf.checkConfigurer(this.ohClass, this.factory);
        this.baseUrls = Elf.checkBaseUrls(this.configurer, this.ohClass, this.factory);
        this.mappingMethodNameDisabled = Elf.checkMappingMethodNameDisabled(this.configurer, this.ohClass);

        this.clientProxy = Elf.checkClientProxy(this.configurer, this.ohClass, this.factory);
        if (this.configurer instanceof ClientSSLConfigurer sslConfigurer) {
            this.sslSocketFactory = sslConfigurer.sslSocketFactory();
            this.x509TrustManager = sslConfigurer.x509TrustManager();
            this.hostnameVerifier = sslConfigurer.hostnameVerifier();
        } else {
            val clientSSL = Elf.checkClientSSL(this.ohClass);
            if (nonNull(clientSSL)) {
                this.sslSocketFactory = Elf.checkSSLSocketFactory(
                        this.ohClass, this.factory, clientSSL);
                this.x509TrustManager = Elf.checkX509TrustManager(
                        this.ohClass, this.factory, clientSSL);
                this.hostnameVerifier = Elf.checkHostnameVerifier(
                        this.ohClass, this.factory, clientSSL);
            }
        }
        this.connectionPool = Elf.checkConnectionPool(this.configurer, this.ohClass);
        if (this.configurer instanceof ClientTimeoutConfigurer timeoutConfigurer) {
            this.callTimeout = timeoutConfigurer.callTimeout();
            this.connectTimeout = timeoutConfigurer.connectTimeout();
            this.readTimeout = timeoutConfigurer.readTimeout();
            this.writeTimeout = timeoutConfigurer.writeTimeout();
        } else {
            val clientTimeout = Elf.checkClientTimeout(this.ohClass);
            if (nonNull(clientTimeout)) {
                this.callTimeout = Elf.checkCallTimeout(
                        this.ohClass, this.factory, clientTimeout);
                this.connectTimeout = Elf.checkConnectTimeout(
                        this.ohClass, this.factory, clientTimeout);
                this.readTimeout = Elf.checkReadTimeout(
                        this.ohClass, this.factory, clientTimeout);
                this.writeTimeout = Elf.checkWriteTimeout(
                        this.ohClass, this.factory, clientTimeout);
            }
        }
        this.interceptors = Elf.checkClientInterceptors(this.configurer, this.ohClass, this.factory);
        this.loggingLevel = Elf.checkClientLoggingLevel(this.configurer, this.ohClass, this.factory);
        this.okHttpClient = Elf.buildOkHttpClient(this);

        this.acceptCharset = Elf.checkAcceptCharset(this.configurer, this.ohClass);
        this.contentFormatter = Elf.checkContentFormatter(this.configurer, this.ohClass, this.factory);
        this.httpMethod = Elf.checkHttpMethod(this.configurer, this.ohClass);
        this.headers = Elf.checkFixedHeaders(this.configurer, this.ohClass, this.factory);
        this.pathVars = Elf.checkFixedPathVars(this.configurer, this.ohClass, this.factory);
        this.parameters = Elf.checkFixedParameters(this.configurer, this.ohClass, this.factory);
        this.contexts = Elf.checkFixedContexts(this.configurer, this.ohClass, this.factory);

        this.statusFallbackMapping = Elf.checkStatusFallbackMapping(this.configurer, this.ohClass);
        this.statusSeriesFallbackMapping = Elf.checkStatusSeriesFallbackMapping(this.configurer, this.ohClass);

        this.requestExtender = Elf.checkRequestExtender(this.configurer, this.ohClass, this.factory);
        this.responseParser = Elf.checkResponseParser(this.configurer, this.ohClass, this.factory);

        this.extraUrlQueryBuilder = Elf.checkExtraUrlQueryBuilder(this.configurer, this.ohClass, this.factory);

        this.mappingBalancer = Elf.checkMappingBalancer(this.configurer, this.ohClass, this.factory);
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

        @SuppressWarnings("DuplicatedCode")
        static Configurer checkConfigurer(Class clazz, Factory factory) {
            val configureWith = getMergedAnnotation(clazz, ConfigureWith.class);
            if (isNull(configureWith)) return null;
            val configurerClass = configureWith.value();
            val configurer = FactoryContext.build(factory, configurerClass);
            if (nonNull(configurer)) return configurer;
            try {
                return ConfigFactory.configLoader(factory).getConfig(configurerClass);
            } catch (Exception e) {
                log.warn("Load Configurer by ConfigService with exception: ", e);
                return null;
            }
        }

        static List<String> checkBaseUrls(Configurer configurer, Class clazz, Factory factory) {
            if (configurer instanceof MappingConfigurer mappingConfigurer)
                return newArrayList(mappingConfigurer.urls())
                        .stream().map(OhDummy::substitute).collect(Collectors.toList());
            val mapping = getMergedAnnotation(clazz, Mapping.class);
            if (isNull(mapping)) return newArrayList("");
            val providerClass = mapping.urlProvider();
            return (UrlProvider.class == providerClass ? Arrays.asList(mapping.value())
                    : FactoryContext.apply(factory, providerClass, p ->
                    emptyThen(p.urls(clazz), () -> newArrayList(p.url(clazz)))))
                    .stream().map(OhDummy::substitute).collect(Collectors.toList());
        }

        static boolean checkMappingMethodNameDisabled(Configurer configurer, Class clazz) {
            if (configurer instanceof MappingMethodNameDisabledConfigurer disabledConfigurer)
                return disabledConfigurer.disabledMappingMethodName();
            return isAnnotated(clazz, MappingMethodNameDisabled.class);
        }

        static Proxy checkClientProxy(Configurer configurer, Class clazz, Factory factory) {
            if (configurer instanceof ClientProxyConfigurer proxyConfigurer)
                return proxyConfigurer.proxy();
            val clientProxy = getMergedAnnotation(clazz, ClientProxy.class);
            return notNullThen(clientProxy, annotation -> {
                val providerClass = annotation.proxyProvider();
                if (ProxyProvider.class == providerClass) {
                    return checkBlank(annotation.host(), () -> null,
                            xx -> new Proxy(annotation.type(), new InetSocketAddress(
                                    annotation.host(), annotation.port())));
                }
                return FactoryContext.apply(factory, providerClass, p -> p.proxy(clazz));
            });
        }

        static ClientSSL checkClientSSL(Class clazz) {
            return getMergedAnnotation(clazz, ClientSSL.class);
        }

        static SSLSocketFactory checkSSLSocketFactory(
                Class clazz, Factory factory, ClientSSL clientSSL) {
            val providerClass = clientSSL.sslSocketFactoryProvider();
            if (SSLSocketFactoryProvider.class == providerClass) {
                val factoryClass = clientSSL.sslSocketFactory();
                return SSLSocketFactory.class == factoryClass ? null
                        : FactoryContext.build(factory, factoryClass);
            }
            return FactoryContext.apply(factory, providerClass,
                    p -> p.sslSocketFactory(clazz));
        }

        static X509TrustManager checkX509TrustManager(
                Class clazz, Factory factory, ClientSSL clientSSL) {
            val providerClass = clientSSL.x509TrustManagerProvider();
            if (X509TrustManagerProvider.class == providerClass) {
                val managerClass = clientSSL.x509TrustManager();
                return X509TrustManager.class == managerClass ? null
                        : FactoryContext.build(factory, managerClass);
            }
            return FactoryContext.apply(factory, providerClass,
                    p -> p.x509TrustManager(clazz));
        }

        static HostnameVerifier checkHostnameVerifier(
                Class clazz, Factory factory, ClientSSL clientSSL) {
            val providerClass = clientSSL.hostnameVerifierProvider();
            if (HostnameVerifierProvider.class == providerClass) {
                val verifierClass = clientSSL.hostnameVerifier();
                return HostnameVerifier.class == verifierClass ? null
                        : FactoryContext.build(factory, verifierClass);
            }
            return FactoryContext.apply(factory, providerClass,
                    p -> p.hostnameVerifier(clazz));
        }

        static ConnectionPool checkConnectionPool(Configurer configurer, Class clazz) {
            val isolated = configurer instanceof IsolatedConnectionPoolConfigurer poolConfigurer
                    ? poolConfigurer.isolatedConnectionPool() : isAnnotated(clazz, IsolatedConnectionPool.class);
            return isolated ? new ConnectionPool() : ohConnectionPool;
        }

        static ClientTimeout checkClientTimeout(Class clazz) {
            return getMergedAnnotation(clazz, ClientTimeout.class);
        }

        static long checkCallTimeout(
                Class clazz, Factory factory, ClientTimeout clientTimeout) {
            val providerClass = clientTimeout.callTimeoutProvider();
            return TimeoutProvider.class == providerClass ? clientTimeout.callTimeout()
                    : FactoryContext.apply(factory, providerClass, p -> p.timeout(clazz));
        }

        static long checkConnectTimeout(
                Class clazz, Factory factory, ClientTimeout clientTimeout) {
            val providerClass = clientTimeout.connectTimeoutProvider();
            return TimeoutProvider.class == providerClass ? clientTimeout.connectTimeout()
                    : FactoryContext.apply(factory, providerClass, p -> p.timeout(clazz));
        }

        static long checkReadTimeout(
                Class clazz, Factory factory, ClientTimeout clientTimeout) {
            val providerClass = clientTimeout.readTimeoutProvider();
            return TimeoutProvider.class == providerClass ? clientTimeout.readTimeout()
                    : FactoryContext.apply(factory, providerClass, p -> p.timeout(clazz));
        }

        static long checkWriteTimeout(
                Class clazz, Factory factory, ClientTimeout clientTimeout) {
            val providerClass = clientTimeout.writeTimeoutProvider();
            return TimeoutProvider.class == providerClass ? clientTimeout.writeTimeout()
                    : FactoryContext.apply(factory, providerClass, p -> p.timeout(clazz));
        }

        static List<Interceptor> checkClientInterceptors(Configurer configurer, Class clazz, Factory factory) {
            if (configurer instanceof ClientInterceptorsConfigurer interceptorsConfigurer)
                return interceptorsConfigurer.interceptors();
            return newArrayList(getMergedRepeatableAnnotations(clazz, ClientInterceptor.class))
                    .stream().filter(annotation -> Interceptor.class != annotation.value()
                            || InterceptorProvider.class != annotation.provider())
                    .map(annotation -> {
                        val providerClass = annotation.provider();
                        if (InterceptorProvider.class == providerClass) {
                            return FactoryContext.build(factory, annotation.value());
                        }
                        return FactoryContext.apply(factory, providerClass, p -> p.interceptor(clazz));
                    }).collect(Collectors.toList());
        }

        static Level checkClientLoggingLevel(Configurer configurer, Class clazz, Factory factory) {
            if (configurer instanceof ClientLoggingLevelConfigurer loggingLevelConfigurer)
                return loggingLevelConfigurer.loggingLevel();
            val clientLoggingLevel = getMergedAnnotation(clazz, ClientLoggingLevel.class);
            if (isNull(clientLoggingLevel)) return DEFAULT_LOGGING_LEVEL;
            val providerClass = clientLoggingLevel.provider();
            return LoggingLevelProvider.class == providerClass ? clientLoggingLevel.value()
                    : FactoryContext.apply(factory, providerClass, p -> p.level(clazz));
        }

        static OkHttpClient buildOkHttpClient(OhProxy proxy) {
            return new OhReq().clientProxy(proxy.clientProxy)
                    .sslSocketFactory(proxy.sslSocketFactory)
                    .x509TrustManager(proxy.x509TrustManager)
                    .hostnameVerifier(proxy.hostnameVerifier)
                    .connectionPool(proxy.connectionPool)
                    .callTimeout(proxy.callTimeout)
                    .connectTimeout(proxy.connectTimeout)
                    .readTimeout(proxy.readTimeout)
                    .writeTimeout(proxy.writeTimeout)
                    .addInterceptors(proxy.interceptors)
                    .loggingLevel(proxy.loggingLevel)
                    .buildHttpClient();
        }

        static Charset checkAcceptCharset(Configurer configurer, Class clazz) {
            if (configurer instanceof AcceptCharsetConfigurer acceptCharsetConfigurer)
                return acceptCharsetConfigurer.acceptCharset();
            val acceptCharset = getMergedAnnotation(clazz, AcceptCharset.class);
            return checkNull(acceptCharset, () -> DEFAULT_ACCEPT_CHARSET,
                    annotation -> Charset.forName(annotation.value()));
        }

        static ContentFormatter checkContentFormatter(Configurer configurer, Class clazz, Factory factory) {
            if (configurer instanceof ContentFormatConfigurer contentFormatConfigurer)
                return contentFormatConfigurer.contentFormatter();
            val contentFormat = getMergedAnnotation(clazz, ContentFormat.class);
            return checkNull(contentFormat, () -> DEFAULT_CONTENT_FORMATTER,
                    annotation -> FactoryContext.build(factory, annotation.value()));
        }

        static HttpMethod checkHttpMethod(Configurer configurer, Class clazz) {
            if (configurer instanceof RequestMethodConfigurer requestMethodConfigurer)
                return requestMethodConfigurer.requestMethod();
            val requestMethod = getMergedAnnotation(clazz, RequestMethod.class);
            return checkNull(requestMethod, () -> DEFAULT_HTTP_METHOD, RequestMethod::value);
        }

        static List<Pair<String, String>> checkFixedHeaders(Configurer configurer, Class clazz, Factory factory) {
            if (configurer instanceof FixedHeadersConfigurer fixedHeadersConfigurer)
                return fixedHeadersConfigurer.fixedHeaders();
            return newArrayList(getMergedRepeatableAnnotations(clazz, FixedHeader.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, FixedValueProvider.class == providerClass
                                ? an.value() : FactoryContext.apply(factory,
                                providerClass, p -> p.value(clazz, name)));
                    }).collect(Collectors.toList());
        }

        static List<Pair<String, String>> checkFixedPathVars(Configurer configurer, Class clazz, Factory factory) {
            if (configurer instanceof FixedPathVarsConfigurer fixedPathVarsConfigurer)
                return fixedPathVarsConfigurer.fixedPathVars();
            return newArrayList(getMergedRepeatableAnnotations(clazz, FixedPathVar.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, FixedValueProvider.class == providerClass
                                ? an.value() : FactoryContext.apply(factory,
                                providerClass, p -> p.value(clazz, name)));
                    }).collect(Collectors.toList());
        }

        static List<Pair<String, Object>> checkFixedParameters(Configurer configurer, Class clazz, Factory factory) {
            if (configurer instanceof FixedParametersConfigurer fixedParametersConfigurer)
                return fixedParametersConfigurer.fixedParameters();
            return newArrayList(getMergedRepeatableAnnotations(clazz, FixedParameter.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, (Object) (FixedValueProvider.class == providerClass
                                ? an.value() : FactoryContext.apply(factory,
                                providerClass, p -> p.value(clazz, name))));
                    }).collect(Collectors.toList());
        }

        static List<Pair<String, Object>> checkFixedContexts(Configurer configurer, Class clazz, Factory factory) {
            if (configurer instanceof FixedContextsConfigurer fixedContextsConfigurer)
                return fixedContextsConfigurer.fixedContexts();
            return newArrayList(getMergedRepeatableAnnotations(clazz, FixedContext.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, (Object) (FixedValueProvider.class == providerClass
                                ? an.value() : FactoryContext.apply(factory,
                                providerClass, p -> p.value(clazz, name))));
                    }).collect(Collectors.toList());
        }

        static Map<HttpStatus, Class<? extends FallbackFunction>>
        checkStatusFallbackMapping(Configurer configurer, Class clazz) {
            if (configurer instanceof StatusFallbacksConfigurer statusFallbacksConfigurer)
                return statusFallbacksConfigurer.statusFallbackMapping();
            return newArrayList(getMergedRepeatableAnnotations(
                    clazz, StatusFallback.class)).stream()
                    .collect(toMap(StatusFallback::status, StatusFallback::fallback));
        }

        static Map<HttpStatus.Series, Class<? extends FallbackFunction>>
        checkStatusSeriesFallbackMapping(Configurer configurer, Class clazz) {
            val disabled = configurer instanceof DefaultFallbackDisabledConfigurer disabledConfigurer
                    ? disabledConfigurer.disabledDefaultFallback() : isAnnotated(clazz, DefaultFallbackDisabled.class);
            Map<HttpStatus.Series, Class<? extends FallbackFunction>> result = disabled ? newHashMap()
                    : of(HttpStatus.Series.CLIENT_ERROR, StatusErrorThrower.class,
                    HttpStatus.Series.SERVER_ERROR, StatusErrorThrower.class);
            if (configurer instanceof StatusSeriesFallbacksConfigurer statusSeriesFallbacksConfigurer) {
                result.putAll(statusSeriesFallbacksConfigurer.statusSeriesFallbackMapping());
            } else {
                result.putAll(newArrayList(getMergedRepeatableAnnotations(clazz,
                        StatusSeriesFallback.class)).stream()
                        .collect(toMap(StatusSeriesFallback::statusSeries, StatusSeriesFallback::fallback)));
            }
            return result;
        }

        static RequestExtender checkRequestExtender(Configurer configurer, Class clazz, Factory factory) {
            if (configurer instanceof RequestExtendConfigurer requestExtendConfigurer)
                return requestExtendConfigurer.requestExtender();
            val requestExtend = getMergedAnnotation(clazz, RequestExtend.class);
            return checkNull(requestExtend, () -> null, annotation ->
                    FactoryContext.build(factory, annotation.value()));
        }

        static ResponseParser checkResponseParser(Configurer configurer, Class clazz, Factory factory) {
            if (configurer instanceof ResponseParseConfigurer responseParseConfigurer)
                return responseParseConfigurer.responseParser();
            val responseParse = getMergedAnnotation(clazz, ResponseParse.class);
            return checkNull(responseParse, () -> null, annotation ->
                    FactoryContext.build(factory, annotation.value()));
        }

        static ExtraUrlQueryBuilder checkExtraUrlQueryBuilder(Configurer configurer, Class clazz, Factory factory) {
            if (configurer instanceof ExtraUrlQueryConfigurer extraUrlQueryConfigurer)
                return extraUrlQueryConfigurer.extraUrlQueryBuilder();
            val extraUrlQuery = getMergedAnnotation(clazz, ExtraUrlQuery.class);
            return checkNull(extraUrlQuery, () -> null, annotation ->
                    FactoryContext.build(factory, annotation.value()));
        }

        static MappingBalancer checkMappingBalancer(Configurer configurer, Class clazz, Factory factory) {
            if (configurer instanceof MappingBalanceConfigurer mappingBalanceConfigurer)
                return mappingBalanceConfigurer.mappingBalancer();
            val mappingBalance = getMergedAnnotation(clazz, MappingBalance.class);
            return checkNull(mappingBalance, RandomBalancer::new, annotation ->
                    FactoryContext.build(factory, annotation.value()));
        }
    }
}
