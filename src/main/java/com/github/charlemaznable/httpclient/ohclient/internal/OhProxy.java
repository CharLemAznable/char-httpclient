package com.github.charlemaznable.httpclient.ohclient.internal;

import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.BuddyEnhancer;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.httpclient.common.*;
import com.github.charlemaznable.httpclient.common.ContentFormat.ContentFormatter;
import com.github.charlemaznable.httpclient.common.ExtraUrlQuery.ExtraUrlQueryBuilder;
import com.github.charlemaznable.httpclient.common.Mapping.UrlProvider;
import com.github.charlemaznable.httpclient.common.MappingBalance.MappingBalancer;
import com.github.charlemaznable.httpclient.common.MappingBalance.RandomBalancer;
import com.github.charlemaznable.httpclient.common.RequestExtend.RequestExtender;
import com.github.charlemaznable.httpclient.common.ResponseParse.ResponseParser;
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
import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
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
import static com.github.charlemaznable.core.spring.AnnotationElf.findAnnotation;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_ACCEPT_CHARSET;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_CONTENT_FORMATTER;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_HTTP_METHOD;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_LOGGING_LEVEL;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhDummy.ohConnectionPool;
import static com.google.common.cache.CacheLoader.from;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedRepeatableAnnotations;

@SuppressWarnings("rawtypes")
public final class OhProxy extends OhRoot implements BuddyEnhancer.Delegate, Reloadable {

    Class ohClass;
    Factory factory;
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
        this.baseUrls = Elf.checkBaseUrls(this.ohClass, this.factory);
        this.mappingMethodNameDisabled = Elf.checkMappingMethodNameDisabled(this.ohClass);

        this.clientProxy = Elf.checkClientProxy(this.ohClass, this.factory);
        val clientSSL = Elf.checkClientSSL(this.ohClass);
        if (nonNull(clientSSL)) {
            this.sslSocketFactory = Elf.checkSSLSocketFactory(
                    this.ohClass, this.factory, clientSSL);
            this.x509TrustManager = Elf.checkX509TrustManager(
                    this.ohClass, this.factory, clientSSL);
            this.hostnameVerifier = Elf.checkHostnameVerifier(
                    this.ohClass, this.factory, clientSSL);
        }
        this.connectionPool = Elf.checkConnectionPool(this.ohClass);
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
        this.interceptors = Elf.checkClientInterceptors(this.ohClass, this.factory);
        this.loggingLevel = Elf.checkClientLoggingLevel(this.ohClass, this.factory);
        this.okHttpClient = Elf.buildOkHttpClient(this);

        this.acceptCharset = Elf.checkAcceptCharset(this.ohClass);
        this.contentFormatter = Elf.checkContentFormatter(this.ohClass, this.factory);
        this.httpMethod = Elf.checkHttpMethod(this.ohClass);
        this.headers = Elf.checkFixedHeaders(this.ohClass, this.factory);
        this.pathVars = Elf.checkFixedPathVars(this.ohClass, this.factory);
        this.parameters = Elf.checkFixedParameters(this.ohClass, this.factory);
        this.contexts = Elf.checkFixedContexts(this.ohClass, this.factory);

        this.statusFallbackMapping = Elf.checkStatusFallbackMapping(this.ohClass);
        this.statusSeriesFallbackMapping = Elf.checkStatusSeriesFallbackMapping(this.ohClass);

        this.requestExtender = Elf.checkRequestExtender(this.ohClass, this.factory);
        this.responseParser = Elf.checkResponseParser(this.ohClass, this.factory);

        this.extraUrlQueryBuilder = Elf.checkExtraUrlQueryBuilder(this.ohClass, this.factory);

        this.mappingBalancer = Elf.checkMappingBalancer(this.ohClass, this.factory);
    }

    private OhMappingProxy loadMappingProxy(Method method) {
        return new OhMappingProxy(this.ohClass, method, this.factory, this);
    }

    @NoArgsConstructor(access = PRIVATE)
    static class Elf {

        static void checkOhClient(Class clazz) {
            checkNotNull(findAnnotation(clazz, OhClient.class),
                    new OhException(clazz.getName() + " has no OhClient annotation"));
        }

        static List<String> checkBaseUrls(Class clazz, Factory factory) {
            val mapping = getMergedAnnotation(clazz, Mapping.class);
            if (isNull(mapping)) return newArrayList("");
            val providerClass = mapping.urlProvider();
            return (UrlProvider.class == providerClass ? Arrays.asList(mapping.value())
                    : FactoryContext.apply(factory, providerClass, p ->
                    emptyThen(p.urls(clazz), () -> newArrayList(p.url(clazz)))))
                    .stream().map(OhDummy::substitute).collect(Collectors.toList());
        }

        static boolean checkMappingMethodNameDisabled(Class clazz) {
            return nonNull(findAnnotation(clazz, MappingMethodNameDisabled.class));
        }

        static Proxy checkClientProxy(Class clazz, Factory factory) {
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

        static ConnectionPool checkConnectionPool(Class clazz) {
            val isolated = findAnnotation(clazz, IsolatedConnectionPool.class);
            return checkNull(isolated, () -> ohConnectionPool, x -> new ConnectionPool());
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

        static List<Interceptor> checkClientInterceptors(Class clazz, Factory factory) {
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

        static Level checkClientLoggingLevel(Class clazz, Factory factory) {
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

        static Charset checkAcceptCharset(Class clazz) {
            val acceptCharset = getMergedAnnotation(clazz, AcceptCharset.class);
            return checkNull(acceptCharset, () -> DEFAULT_ACCEPT_CHARSET,
                    annotation -> Charset.forName(annotation.value()));
        }

        static ContentFormatter checkContentFormatter(Class clazz, Factory factory) {
            val contentFormat = getMergedAnnotation(clazz, ContentFormat.class);
            return checkNull(contentFormat, () -> DEFAULT_CONTENT_FORMATTER,
                    annotation -> FactoryContext.build(factory, annotation.value()));
        }

        static HttpMethod checkHttpMethod(Class clazz) {
            val requestMethod = getMergedAnnotation(clazz, RequestMethod.class);
            return checkNull(requestMethod, () -> DEFAULT_HTTP_METHOD, RequestMethod::value);
        }

        static List<Pair<String, String>> checkFixedHeaders(Class clazz, Factory factory) {
            return newArrayList(getMergedRepeatableAnnotations(clazz, FixedHeader.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, FixedValueProvider.class == providerClass
                                ? an.value() : FactoryContext.apply(factory,
                                providerClass, p -> p.value(clazz, name)));
                    }).collect(Collectors.toList());
        }

        static List<Pair<String, String>> checkFixedPathVars(Class clazz, Factory factory) {
            return newArrayList(getMergedRepeatableAnnotations(clazz, FixedPathVar.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, FixedValueProvider.class == providerClass
                                ? an.value() : FactoryContext.apply(factory,
                                providerClass, p -> p.value(clazz, name)));
                    }).collect(Collectors.toList());
        }

        static List<Pair<String, Object>> checkFixedParameters(Class clazz, Factory factory) {
            return newArrayList(getMergedRepeatableAnnotations(clazz, FixedParameter.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, (Object) (FixedValueProvider.class == providerClass
                                ? an.value() : FactoryContext.apply(factory,
                                providerClass, p -> p.value(clazz, name))));
                    }).collect(Collectors.toList());
        }

        static List<Pair<String, Object>> checkFixedContexts(Class clazz, Factory factory) {
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
        checkStatusFallbackMapping(Class clazz) {
            return newArrayList(getMergedRepeatableAnnotations(
                    clazz, StatusFallback.class)).stream()
                    .collect(toMap(StatusFallback::status, StatusFallback::fallback));
        }

        static Map<HttpStatus.Series, Class<? extends FallbackFunction>>
        checkStatusSeriesFallbackMapping(Class clazz) {
            val defaultDisabled = findAnnotation(clazz, DefaultFallbackDisabled.class);
            Map<HttpStatus.Series, Class<? extends FallbackFunction>> result = checkNull(
                    defaultDisabled, () -> of(HttpStatus.Series.CLIENT_ERROR, StatusErrorThrower.class,
                            HttpStatus.Series.SERVER_ERROR, StatusErrorThrower.class), x -> newHashMap());
            result.putAll(newArrayList(getMergedRepeatableAnnotations(clazz,
                    StatusSeriesFallback.class)).stream()
                    .collect(toMap(StatusSeriesFallback::statusSeries, StatusSeriesFallback::fallback)));
            return result;
        }

        static RequestExtender checkRequestExtender(Class clazz, Factory factory) {
            val requestExtend = getMergedAnnotation(clazz, RequestExtend.class);
            return checkNull(requestExtend, () -> null, annotation ->
                    FactoryContext.build(factory, annotation.value()));
        }

        static ResponseParser checkResponseParser(Class clazz, Factory factory) {
            val responseParse = getMergedAnnotation(clazz, ResponseParse.class);
            return checkNull(responseParse, () -> null, annotation ->
                    FactoryContext.build(factory, annotation.value()));
        }

        static ExtraUrlQueryBuilder checkExtraUrlQueryBuilder(Class clazz, Factory factory) {
            val extraUrlQuery = getMergedAnnotation(clazz, ExtraUrlQuery.class);
            return checkNull(extraUrlQuery, () -> null, annotation ->
                    FactoryContext.build(factory, annotation.value()));
        }

        static MappingBalancer checkMappingBalancer(Class clazz, Factory factory) {
            val mappingBalance = getMergedAnnotation(clazz, MappingBalance.class);
            return checkNull(mappingBalance, RandomBalancer::new, annotation ->
                    FactoryContext.build(factory, annotation.value()));
        }
    }
}
