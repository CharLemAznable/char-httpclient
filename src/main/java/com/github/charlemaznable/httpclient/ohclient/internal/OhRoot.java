package com.github.charlemaznable.httpclient.ohclient.internal;

import com.github.charlemaznable.configservice.ConfigFactory;
import com.github.charlemaznable.configservice.ConfigListenerRegister;
import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.Factory;
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
import com.github.charlemaznable.httpclient.ohclient.annotation.IsolatedDispatcher;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientInterceptorsConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientLoggingLevelConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientProxyConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientSSLConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientTimeoutConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.IsolatedConnectionPoolConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.IsolatedDispatcherConfigurer;
import com.github.charlemaznable.httpclient.ohclient.elf.OhConnectionPoolElf;
import com.github.charlemaznable.httpclient.ohclient.elf.OhDispatcherElf;
import lombok.val;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.github.charlemaznable.core.lang.Condition.emptyThen;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.notNullThenRun;
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
    Dispatcher dispatcher;
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

    static void checkConfigurerIsRegisterThenRun(Configurer configurer,
                                                 Consumer<ConfigListenerRegister> consumer) {
        if (configurer instanceof ConfigListenerRegister) {
            notNullThenRun(consumer, c -> c.accept(((ConfigListenerRegister) configurer)));
        }
    }

    static List<String> checkMappingUrls(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof MappingConfigurer)
            return newArrayList(((MappingConfigurer) configurer).urls())
                    .stream().map(OhDummy::substitute).collect(Collectors.toList());
        val mapping = getMergedAnnotation(element, Mapping.class);
        return notNullThen(mapping, anno -> Arrays
                .stream(anno.value()).map(OhDummy::substitute).collect(Collectors.toList()));
    }

    static Charset checkAcceptCharset(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof AcceptCharsetConfigurer)
            return ((AcceptCharsetConfigurer) configurer).acceptCharset();
        val acceptCharset = getMergedAnnotation(element, AcceptCharset.class);
        return notNullThen(acceptCharset, anno -> Charset.forName(anno.value()));
    }

    static ContentFormatter checkContentFormatter(Configurer configurer, AnnotatedElement element, Factory factory) {
        if (configurer instanceof ContentFormatConfigurer)
            return ((ContentFormatConfigurer) configurer).contentFormatter();
        val contentFormat = getMergedAnnotation(element, ContentFormat.class);
        return notNullThen(contentFormat, anno -> FactoryContext.build(factory, anno.value()));
    }

    static HttpMethod checkHttpMethod(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof RequestMethodConfigurer)
            return ((RequestMethodConfigurer) configurer).requestMethod();
        val requestMethod = getMergedAnnotation(element, RequestMethod.class);
        return notNullThen(requestMethod, RequestMethod::value);
    }

    static List<Pair<String, String>> checkFixedHeaders(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof FixedHeadersConfigurer)
            return newArrayList(((FixedHeadersConfigurer) configurer).fixedHeaders())
                    .stream().filter(NOT_BLANK_KEY).collect(Collectors.toList());
        return newArrayList(getMergedRepeatableAnnotations(element, FixedHeader.class))
                .stream().map(anno -> Pair.of(anno.name(), cleanupValue(
                        anno.value(), anno.emptyAsCleanup()))).filter(NOT_BLANK_KEY).collect(Collectors.toList());
    }

    static List<Pair<String, String>> checkFixedPathVars(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof FixedPathVarsConfigurer)
            return newArrayList(((FixedPathVarsConfigurer) configurer).fixedPathVars())
                    .stream().filter(NOT_BLANK_KEY).collect(Collectors.toList());
        return newArrayList(getMergedRepeatableAnnotations(element, FixedPathVar.class))
                .stream().map(anno -> Pair.of(anno.name(), cleanupValue(
                        anno.value(), anno.emptyAsCleanup()))).filter(NOT_BLANK_KEY).collect(Collectors.toList());
    }

    static List<Pair<String, Object>> checkFixedParameters(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof FixedParametersConfigurer)
            return newArrayList(((FixedParametersConfigurer) configurer).fixedParameters())
                    .stream().filter(NOT_BLANK_KEY).collect(Collectors.toList());
        return newArrayList(getMergedRepeatableAnnotations(element, FixedParameter.class))
                .stream().map(anno -> Pair.of(anno.name(), (Object) cleanupValue(
                        anno.value(), anno.emptyAsCleanup()))).filter(NOT_BLANK_KEY).collect(Collectors.toList());
    }

    static List<Pair<String, Object>> checkFixedContexts(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof FixedContextsConfigurer)
            return newArrayList(((FixedContextsConfigurer) configurer).fixedContexts())
                    .stream().filter(NOT_BLANK_KEY).collect(Collectors.toList());
        return newArrayList(getMergedRepeatableAnnotations(element, FixedContext.class))
                .stream().map(anno -> Pair.of(anno.name(), (Object) cleanupValue(
                        anno.value(), anno.emptyAsCleanup()))).filter(NOT_BLANK_KEY).collect(Collectors.toList());
    }

    private static String cleanupValue(String value, boolean emptyAsCleanup) {
        return emptyThen(value, () -> emptyAsCleanup ? null : "");
    }

    static Map<HttpStatus, Class<? extends FallbackFunction>>
    checkStatusFallbackMapping(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof StatusFallbacksConfigurer)
            return newHashMap(((StatusFallbacksConfigurer) configurer).statusFallbackMapping());
        return newArrayList(getMergedRepeatableAnnotations(element, StatusFallback.class))
                .stream().collect(toMap(StatusFallback::status, StatusFallback::fallback));
    }

    static Map<HttpStatus.Series, Class<? extends FallbackFunction>>
    checkStatusSeriesFallbackMapping(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof StatusSeriesFallbacksConfigurer)
            return newHashMap(((StatusSeriesFallbacksConfigurer) configurer).statusSeriesFallbackMapping());
        return newArrayList(getMergedRepeatableAnnotations(element, StatusSeriesFallback.class))
                .stream().collect(toMap(StatusSeriesFallback::statusSeries, StatusSeriesFallback::fallback));
    }

    static RequestExtender checkRequestExtender(Configurer configurer, AnnotatedElement element, Factory factory) {
        if (configurer instanceof RequestExtendConfigurer)
            return ((RequestExtendConfigurer) configurer).requestExtender();
        val requestExtend = getMergedAnnotation(element, RequestExtend.class);
        return notNullThen(requestExtend, anno -> FactoryContext.build(factory, anno.value()));
    }

    static ResponseParser checkResponseParser(Configurer configurer, AnnotatedElement element, Factory factory) {
        if (configurer instanceof ResponseParseConfigurer)
            return ((ResponseParseConfigurer) configurer).responseParser();
        val responseParse = getMergedAnnotation(element, ResponseParse.class);
        return notNullThen(responseParse, anno -> FactoryContext.build(factory, anno.value()));
    }

    static ExtraUrlQueryBuilder checkExtraUrlQueryBuilder(Configurer configurer, AnnotatedElement element, Factory factory) {
        if (configurer instanceof ExtraUrlQueryConfigurer)
            return ((ExtraUrlQueryConfigurer) configurer).extraUrlQueryBuilder();
        val extraUrlQuery = getMergedAnnotation(element, ExtraUrlQuery.class);
        return notNullThen(extraUrlQuery, anno -> FactoryContext.build(factory, anno.value()));
    }

    static MappingBalancer checkMappingBalancer(Configurer configurer, AnnotatedElement element, Factory factory) {
        if (configurer instanceof MappingBalanceConfigurer)
            return ((MappingBalanceConfigurer) configurer).mappingBalancer();
        val mappingBalance = getMergedAnnotation(element, MappingBalance.class);
        return notNullThen(mappingBalance, anno -> FactoryContext.build(factory, anno.value()));
    }

    static Proxy checkClientProxy(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof ClientProxyConfigurer)
            return ((ClientProxyConfigurer) configurer).proxy();
        val clientProxy = getMergedAnnotation(element, ClientProxy.class);
        return notNullThen(clientProxy, anno -> new Proxy(anno.type(),
                new InetSocketAddress(anno.host(), anno.port())));
    }

    static SSLRoot checkClientSSL(Configurer configurer, AnnotatedElement element, Factory factory,
                                  boolean disabledSSLSocketFactory,
                                  boolean disabledX509TrustManager,
                                  boolean disabledHostnameVerifier, SSLRoot defaultValue) {
        val sslRoot = new SSLRoot();
        if (configurer instanceof ClientSSLConfigurer) {
            processSSLRootWithConfigurer(sslRoot, (ClientSSLConfigurer) configurer, disabledSSLSocketFactory,
                    disabledX509TrustManager, disabledHostnameVerifier, defaultValue);
        } else {
            val clientSSL = getMergedAnnotation(element, ClientSSL.class);
            if (nonNull(clientSSL)) {
                processSSLRootWithAnnotation(sslRoot, clientSSL, factory, disabledSSLSocketFactory,
                        disabledX509TrustManager, disabledHostnameVerifier, defaultValue);
            } else {
                processSSLRootWithDefault(sslRoot, disabledSSLSocketFactory,
                        disabledX509TrustManager, disabledHostnameVerifier, defaultValue);
            }
        }
        return sslRoot;
    }

    private static void processSSLRootWithConfigurer(SSLRoot sslRoot, ClientSSLConfigurer sslConfigurer,
                                                     boolean disabledSSLSocketFactory,
                                                     boolean disabledX509TrustManager,
                                                     boolean disabledHostnameVerifier, SSLRoot defaultValue) {
        sslRoot.sslSocketFactory = disabledSSLSocketFactory ? null : nullThen(
                sslConfigurer.sslSocketFactory(), () -> defaultValue.sslSocketFactory);
        sslRoot.x509TrustManager = disabledX509TrustManager ? null : nullThen(
                sslConfigurer.x509TrustManager(), () -> defaultValue.x509TrustManager);
        sslRoot.hostnameVerifier = disabledHostnameVerifier ? null : nullThen(
                sslConfigurer.hostnameVerifier(), () -> defaultValue.hostnameVerifier);
    }

    private static void processSSLRootWithAnnotation(SSLRoot sslRoot, ClientSSL clientSSL, Factory factory,
                                                     boolean disabledSSLSocketFactory,
                                                     boolean disabledX509TrustManager,
                                                     boolean disabledHostnameVerifier, SSLRoot defaultValue) {
        sslRoot.sslSocketFactory = disabledSSLSocketFactory ? null : nullThen(
                FactoryContext.build(factory, clientSSL.sslSocketFactory()), () -> defaultValue.sslSocketFactory);
        sslRoot.x509TrustManager = disabledX509TrustManager ? null : nullThen(
                FactoryContext.build(factory, clientSSL.x509TrustManager()), () -> defaultValue.x509TrustManager);
        sslRoot.hostnameVerifier = disabledHostnameVerifier ? null : nullThen(
                FactoryContext.build(factory, clientSSL.hostnameVerifier()), () -> defaultValue.hostnameVerifier);
    }

    private static void processSSLRootWithDefault(SSLRoot sslRoot,
                                                  boolean disabledSSLSocketFactory,
                                                  boolean disabledX509TrustManager,
                                                  boolean disabledHostnameVerifier, SSLRoot defaultValue) {
        sslRoot.sslSocketFactory = disabledSSLSocketFactory ? null : defaultValue.sslSocketFactory;
        sslRoot.x509TrustManager = disabledX509TrustManager ? null : defaultValue.x509TrustManager;
        sslRoot.hostnameVerifier = disabledHostnameVerifier ? null : defaultValue.hostnameVerifier;
    }

    static Dispatcher checkDispatcher(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof IsolatedDispatcherConfigurer) {
            val dispatcherConfigurer = (IsolatedDispatcherConfigurer) configurer;
            return dispatcherConfigurer.isolatedDispatcher() ?
                    nullThen(dispatcherConfigurer.customDispatcher(), OhDispatcherElf::newDispatcher) : null;
        } else {
            return isAnnotated(element, IsolatedDispatcher.class) ? OhDispatcherElf.newDispatcher() : null;
        }
    }

    static ConnectionPool checkConnectionPool(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof IsolatedConnectionPoolConfigurer) {
            val poolConfigurer = (IsolatedConnectionPoolConfigurer) configurer;
            return poolConfigurer.isolatedConnectionPool() ?
                    nullThen(poolConfigurer.customConnectionPool(), OhConnectionPoolElf::newConnectionPool) : null;
        } else {
            return isAnnotated(element, IsolatedConnectionPool.class) ? OhConnectionPoolElf.newConnectionPool() : null;
        }
    }

    static TimeoutRoot checkClientTimeout(Configurer configurer, AnnotatedElement element, TimeoutRoot defaultValue) {
        val timeoutRoot = new TimeoutRoot();
        if (configurer instanceof ClientTimeoutConfigurer) {
            val timeoutConfigurer = (ClientTimeoutConfigurer) configurer;
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
        if (configurer instanceof ClientInterceptorsConfigurer)
            return newArrayList(((ClientInterceptorsConfigurer) configurer).interceptors());
        return newArrayList(getMergedRepeatableAnnotations(element, ClientInterceptor.class))
                .stream().map(anno -> (Interceptor) FactoryContext.build(factory, anno.value())).collect(Collectors.toList());
    }

    static Level checkClientLoggingLevel(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof ClientLoggingLevelConfigurer)
            return ((ClientLoggingLevelConfigurer) configurer).loggingLevel();
        val clientLoggingLevel = getMergedAnnotation(element, ClientLoggingLevel.class);
        return notNullThen(clientLoggingLevel, ClientLoggingLevel::value);
    }

    static OkHttpClient buildOkHttpClient(OhRoot root) {
        return new OhReq().clientProxy(root.clientProxy)
                .sslSocketFactory(root.sslRoot.sslSocketFactory)
                .x509TrustManager(root.sslRoot.x509TrustManager)
                .hostnameVerifier(root.sslRoot.hostnameVerifier)
                .dispatcher(root.dispatcher)
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
