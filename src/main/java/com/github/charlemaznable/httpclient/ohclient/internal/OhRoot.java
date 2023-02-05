package com.github.charlemaznable.httpclient.ohclient.internal;

import com.github.charlemaznable.configservice.ConfigFactory;
import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.core.lang.Listt;
import com.github.charlemaznable.httpclient.common.AcceptCharset;
import com.github.charlemaznable.httpclient.common.ConfigureWith;
import com.github.charlemaznable.httpclient.common.ContentFormat;
import com.github.charlemaznable.httpclient.common.ContentFormat.ContentFormatter;
import com.github.charlemaznable.httpclient.common.ExtraUrlQuery;
import com.github.charlemaznable.httpclient.common.ExtraUrlQuery.ExtraUrlQueryBuilder;
import com.github.charlemaznable.httpclient.common.FallbackFunction;
import com.github.charlemaznable.httpclient.common.FixedContext;
import com.github.charlemaznable.httpclient.common.FixedHeader;
import com.github.charlemaznable.httpclient.common.FixedParameter;
import com.github.charlemaznable.httpclient.common.FixedPathVar;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.common.MappingBalance;
import com.github.charlemaznable.httpclient.common.MappingBalance.MappingBalancer;
import com.github.charlemaznable.httpclient.common.RequestExtend;
import com.github.charlemaznable.httpclient.common.RequestExtend.RequestExtender;
import com.github.charlemaznable.httpclient.common.RequestMethod;
import com.github.charlemaznable.httpclient.common.ResponseParse;
import com.github.charlemaznable.httpclient.common.ResponseParse.ResponseParser;
import com.github.charlemaznable.httpclient.common.StatusFallback;
import com.github.charlemaznable.httpclient.common.StatusSeriesFallback;
import com.github.charlemaznable.httpclient.configurer.AcceptCharsetConfigurer;
import com.github.charlemaznable.httpclient.configurer.Configurer;
import com.github.charlemaznable.httpclient.configurer.ContentFormatConfigurer;
import com.github.charlemaznable.httpclient.configurer.ExtraUrlQueryConfigurer;
import com.github.charlemaznable.httpclient.configurer.FixedContextsConfigurer;
import com.github.charlemaznable.httpclient.configurer.FixedHeadersConfigurer;
import com.github.charlemaznable.httpclient.configurer.FixedParametersConfigurer;
import com.github.charlemaznable.httpclient.configurer.FixedPathVarsConfigurer;
import com.github.charlemaznable.httpclient.configurer.MappingBalanceConfigurer;
import com.github.charlemaznable.httpclient.configurer.MappingConfigurer;
import com.github.charlemaznable.httpclient.configurer.RequestExtendConfigurer;
import com.github.charlemaznable.httpclient.configurer.RequestMethodConfigurer;
import com.github.charlemaznable.httpclient.configurer.ResponseParseConfigurer;
import com.github.charlemaznable.httpclient.configurer.StatusFallbacksConfigurer;
import com.github.charlemaznable.httpclient.configurer.StatusSeriesFallbacksConfigurer;
import com.github.charlemaznable.httpclient.ohclient.OhReq;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientInterceptor;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientLoggingLevel;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientProxy;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientSSL;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientTimeout;
import com.github.charlemaznable.httpclient.ohclient.annotation.IsolatedConnectionPool;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientInterceptorsConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientLoggingLevelConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientProxyConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientSSLConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientTimeoutConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.IsolatedConnectionPoolConfigurer;
import lombok.val;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import org.apache.commons.lang3.tuple.Pair;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.reflect.AnnotatedElement;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.emptyThen;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Mapp.toMap;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_CALL_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_CONNECT_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_READ_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_WRITE_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.NOT_BLANK_KEY;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhDummy.log;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedRepeatableAnnotations;
import static org.springframework.core.annotation.AnnotatedElementUtils.isAnnotated;

@SuppressWarnings("rawtypes")
class OhRoot {

    Proxy clientProxy;
    SSLRoot sslRoot;
    ConnectionPool connectionPool;
    TimeoutRoot timeoutRoot;
    List<Interceptor> interceptors;
    Level loggingLevel;
    OkHttpClient okHttpClient;

    Charset acceptCharset;
    ContentFormatter contentFormatter;
    HttpMethod httpMethod;
    List<Pair<String, String>> headers;
    List<Pair<String, String>> pathVars;
    List<Pair<String, Object>> parameters;
    List<Pair<String, Object>> contexts;

    Map<HttpStatus, Class<? extends FallbackFunction>> statusFallbackMapping;
    Map<HttpStatus.Series, Class<? extends FallbackFunction>> statusSeriesFallbackMapping;

    RequestExtender requestExtender;
    ResponseParser responseParser;

    ExtraUrlQueryBuilder extraUrlQueryBuilder;

    MappingBalancer mappingBalancer;

    static class SSLRoot {

        SSLSocketFactory sslSocketFactory;
        X509TrustManager x509TrustManager;
        HostnameVerifier hostnameVerifier;
    }

    static class TimeoutRoot {

        long callTimeout = DEFAULT_CALL_TIMEOUT; // in milliseconds
        long connectTimeout = DEFAULT_CONNECT_TIMEOUT; // in milliseconds
        long readTimeout = DEFAULT_READ_TIMEOUT; // in milliseconds
        long writeTimeout = DEFAULT_WRITE_TIMEOUT; // in milliseconds
    }

    static Configurer checkConfigurer(AnnotatedElement element, Factory factory) {
        val configureWith = getMergedAnnotation(element, ConfigureWith.class);
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

    static List<String> checkMappingUrls(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof MappingConfigurer mappingConfigurer)
            return newArrayList(mappingConfigurer.urls())
                    .stream().map(OhDummy::substitute).toList();
        val mapping = getMergedAnnotation(element, Mapping.class);
        return checkNull(mapping, Listt::newArrayList, anno -> Arrays
                .stream(anno.value()).map(OhDummy::substitute).toList());
    }

    static Charset checkAcceptCharset(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof AcceptCharsetConfigurer acceptCharsetConfigurer)
            return acceptCharsetConfigurer.acceptCharset();
        val acceptCharset = getMergedAnnotation(element, AcceptCharset.class);
        return notNullThen(acceptCharset, anno -> Charset.forName(anno.value()));
    }

    static ContentFormatter checkContentFormatter(Configurer configurer, AnnotatedElement element, Factory factory) {
        if (configurer instanceof ContentFormatConfigurer contentFormatConfigurer)
            return contentFormatConfigurer.contentFormatter();
        val contentFormat = getMergedAnnotation(element, ContentFormat.class);
        return notNullThen(contentFormat, anno -> FactoryContext.build(factory, anno.value()));
    }

    static HttpMethod checkHttpMethod(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof RequestMethodConfigurer requestMethodConfigurer)
            return requestMethodConfigurer.requestMethod();
        val requestMethod = getMergedAnnotation(element, RequestMethod.class);
        return notNullThen(requestMethod, RequestMethod::value);
    }

    static List<Pair<String, String>> checkFixedHeaders(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof FixedHeadersConfigurer fixedHeadersConfigurer)
            return newArrayList(fixedHeadersConfigurer.fixedHeaders()).stream().filter(NOT_BLANK_KEY).toList();
        return newArrayList(getMergedRepeatableAnnotations(element, FixedHeader.class))
                .stream().map(anno -> Pair.of(anno.name(), cleanupValue(
                        anno.value(), anno.emptyAsCleanup()))).filter(NOT_BLANK_KEY).toList();
    }

    static List<Pair<String, String>> checkFixedPathVars(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof FixedPathVarsConfigurer fixedPathVarsConfigurer)
            return newArrayList(fixedPathVarsConfigurer.fixedPathVars()).stream().filter(NOT_BLANK_KEY).toList();
        return newArrayList(getMergedRepeatableAnnotations(element, FixedPathVar.class))
                .stream().map(anno -> Pair.of(anno.name(), cleanupValue(
                        anno.value(), anno.emptyAsCleanup()))).filter(NOT_BLANK_KEY).toList();
    }

    static List<Pair<String, Object>> checkFixedParameters(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof FixedParametersConfigurer fixedParametersConfigurer)
            return newArrayList(fixedParametersConfigurer.fixedParameters()).stream().filter(NOT_BLANK_KEY).toList();
        return newArrayList(getMergedRepeatableAnnotations(element, FixedParameter.class))
                .stream().map(anno -> Pair.of(anno.name(), (Object) cleanupValue(
                        anno.value(), anno.emptyAsCleanup()))).filter(NOT_BLANK_KEY).toList();
    }

    static List<Pair<String, Object>> checkFixedContexts(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof FixedContextsConfigurer fixedContextsConfigurer)
            return newArrayList(fixedContextsConfigurer.fixedContexts()).stream().filter(NOT_BLANK_KEY).toList();
        return newArrayList(getMergedRepeatableAnnotations(element, FixedContext.class))
                .stream().map(anno -> Pair.of(anno.name(), (Object) cleanupValue(
                        anno.value(), anno.emptyAsCleanup()))).filter(NOT_BLANK_KEY).toList();
    }

    static String cleanupValue(String value, boolean emptyAsCleanup) {
        return emptyThen(value, () -> emptyAsCleanup ? null : "");
    }

    static Map<HttpStatus, Class<? extends FallbackFunction>>
    checkStatusFallbackMapping(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof StatusFallbacksConfigurer statusFallbacksConfigurer)
            return newHashMap(statusFallbacksConfigurer.statusFallbackMapping());
        return newArrayList(getMergedRepeatableAnnotations(element, StatusFallback.class))
                .stream().collect(toMap(StatusFallback::status, StatusFallback::fallback));
    }

    static Map<HttpStatus.Series, Class<? extends FallbackFunction>>
    checkStatusSeriesFallbackMapping(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof StatusSeriesFallbacksConfigurer statusSeriesFallbacksConfigurer)
            return newHashMap(statusSeriesFallbacksConfigurer.statusSeriesFallbackMapping());
        return newArrayList(getMergedRepeatableAnnotations(element, StatusSeriesFallback.class))
                .stream().collect(toMap(StatusSeriesFallback::statusSeries, StatusSeriesFallback::fallback));
    }

    static RequestExtender checkRequestExtender(Configurer configurer, AnnotatedElement element, Factory factory) {
        if (configurer instanceof RequestExtendConfigurer requestExtendConfigurer)
            return requestExtendConfigurer.requestExtender();
        val requestExtend = getMergedAnnotation(element, RequestExtend.class);
        return notNullThen(requestExtend, anno -> FactoryContext.build(factory, anno.value()));
    }

    static ResponseParser checkResponseParser(Configurer configurer, AnnotatedElement element, Factory factory) {
        if (configurer instanceof ResponseParseConfigurer responseParseConfigurer)
            return responseParseConfigurer.responseParser();
        val responseParse = getMergedAnnotation(element, ResponseParse.class);
        return notNullThen(responseParse, anno -> FactoryContext.build(factory, anno.value()));
    }

    static ExtraUrlQueryBuilder checkExtraUrlQueryBuilder(Configurer configurer, AnnotatedElement element, Factory factory) {
        if (configurer instanceof ExtraUrlQueryConfigurer extraUrlQueryConfigurer)
            return extraUrlQueryConfigurer.extraUrlQueryBuilder();
        val extraUrlQuery = getMergedAnnotation(element, ExtraUrlQuery.class);
        return notNullThen(extraUrlQuery, anno -> FactoryContext.build(factory, anno.value()));
    }

    static MappingBalancer checkMappingBalancer(Configurer configurer, AnnotatedElement element, Factory factory) {
        if (configurer instanceof MappingBalanceConfigurer mappingBalanceConfigurer)
            return mappingBalanceConfigurer.mappingBalancer();
        val mappingBalance = getMergedAnnotation(element, MappingBalance.class);
        return notNullThen(mappingBalance, anno -> FactoryContext.build(factory, anno.value()));
    }

    static Proxy checkClientProxy(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof ClientProxyConfigurer proxyConfigurer)
            return proxyConfigurer.proxy();
        val clientProxy = getMergedAnnotation(element, ClientProxy.class);
        return notNullThen(clientProxy, anno -> new Proxy(anno.type(),
                new InetSocketAddress(anno.host(), anno.port())));
    }

    static SSLRoot checkClientSSL(Configurer configurer, AnnotatedElement element, Factory factory,
                                  boolean disabledSSLSocketFactory,
                                  boolean disabledX509TrustManager,
                                  boolean disabledHostnameVerifier, SSLRoot defaultValue) {
        val sslRoot = new SSLRoot();
        if (configurer instanceof ClientSSLConfigurer sslConfigurer) {
            sslRoot.sslSocketFactory = disabledSSLSocketFactory ? null : nullThen(
                    sslConfigurer.sslSocketFactory(), () -> defaultValue.sslSocketFactory);
            sslRoot.x509TrustManager = disabledX509TrustManager ? null : nullThen(
                    sslConfigurer.x509TrustManager(), () -> defaultValue.x509TrustManager);
            sslRoot.hostnameVerifier = disabledHostnameVerifier ? null : nullThen(
                    sslConfigurer.hostnameVerifier(), () -> defaultValue.hostnameVerifier);
        } else {
            val clientSSL = getMergedAnnotation(element, ClientSSL.class);
            if (nonNull(clientSSL)) {
                sslRoot.sslSocketFactory = disabledSSLSocketFactory ? null : nullThen(
                        FactoryContext.build(factory, clientSSL.sslSocketFactory()), () -> defaultValue.sslSocketFactory);
                sslRoot.x509TrustManager = disabledX509TrustManager ? null : nullThen(
                        FactoryContext.build(factory, clientSSL.x509TrustManager()), () -> defaultValue.x509TrustManager);
                sslRoot.hostnameVerifier = disabledHostnameVerifier ? null : nullThen(
                        FactoryContext.build(factory, clientSSL.hostnameVerifier()), () -> defaultValue.hostnameVerifier);
            } else {
                sslRoot.sslSocketFactory = disabledSSLSocketFactory ? null : defaultValue.sslSocketFactory;
                sslRoot.x509TrustManager = disabledX509TrustManager ? null : defaultValue.x509TrustManager;
                sslRoot.hostnameVerifier = disabledHostnameVerifier ? null : defaultValue.hostnameVerifier;
            }
        }
        return sslRoot;
    }

    static ConnectionPool checkConnectionPool(Configurer configurer, AnnotatedElement element) {
        val isolated = configurer instanceof IsolatedConnectionPoolConfigurer poolConfigurer
                ? poolConfigurer.isolatedConnectionPool() : isAnnotated(element, IsolatedConnectionPool.class);
        return isolated ? new ConnectionPool() : null;
    }

    static TimeoutRoot checkClientTimeout(Configurer configurer, AnnotatedElement element, TimeoutRoot defaultValue) {
        val timeoutRoot = new TimeoutRoot();
        if (configurer instanceof ClientTimeoutConfigurer timeoutConfigurer) {
            timeoutRoot.callTimeout = timeoutConfigurer.callTimeout();
            timeoutRoot.connectTimeout = timeoutConfigurer.connectTimeout();
            timeoutRoot.readTimeout = timeoutConfigurer.readTimeout();
            timeoutRoot.writeTimeout = timeoutConfigurer.writeTimeout();
        } else {
            val clientTimeout = getMergedAnnotation(element, ClientTimeout.class);
            if (nonNull(clientTimeout)) {
                timeoutRoot.callTimeout = clientTimeout.callTimeout();
                timeoutRoot.connectTimeout = clientTimeout.connectTimeout();
                timeoutRoot.readTimeout = clientTimeout.readTimeout();
                timeoutRoot.writeTimeout = clientTimeout.writeTimeout();
            } else {
                timeoutRoot.callTimeout = defaultValue.callTimeout;
                timeoutRoot.connectTimeout = defaultValue.connectTimeout;
                timeoutRoot.readTimeout = defaultValue.readTimeout;
                timeoutRoot.writeTimeout = defaultValue.writeTimeout;
            }
        }
        return timeoutRoot;
    }

    static List<Interceptor> checkClientInterceptors(Configurer configurer, AnnotatedElement element, Factory factory) {
        if (configurer instanceof ClientInterceptorsConfigurer interceptorsConfigurer)
            return newArrayList(interceptorsConfigurer.interceptors());
        return newArrayList(getMergedRepeatableAnnotations(element, ClientInterceptor.class))
                .stream().map(anno -> (Interceptor) FactoryContext.build(factory, anno.value())).toList();
    }

    static Level checkClientLoggingLevel(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof ClientLoggingLevelConfigurer loggingLevelConfigurer)
            return loggingLevelConfigurer.loggingLevel();
        val clientLoggingLevel = getMergedAnnotation(element, ClientLoggingLevel.class);
        return notNullThen(clientLoggingLevel, ClientLoggingLevel::value);
    }

    static OkHttpClient buildOkHttpClient(OhRoot root) {
        return new OhReq().clientProxy(root.clientProxy)
                .sslSocketFactory(root.sslRoot.sslSocketFactory)
                .x509TrustManager(root.sslRoot.x509TrustManager)
                .hostnameVerifier(root.sslRoot.hostnameVerifier)
                .connectionPool(root.connectionPool)
                .callTimeout(root.timeoutRoot.callTimeout)
                .connectTimeout(root.timeoutRoot.connectTimeout)
                .readTimeout(root.timeoutRoot.readTimeout)
                .writeTimeout(root.timeoutRoot.writeTimeout)
                .addInterceptors(root.interceptors)
                .loggingLevel(root.loggingLevel)
                .buildHttpClient();
    }
}
