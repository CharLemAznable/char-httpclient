package com.github.charlemaznable.httpclient.ohclient.internal;

import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.httpclient.configurer.Configurer;
import com.github.charlemaznable.httpclient.internal.CommonRoot;
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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.reflect.AnnotatedElement;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_CALL_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_CONNECT_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_READ_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_WRITE_TIMEOUT;
import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedRepeatableAnnotations;
import static org.springframework.core.annotation.AnnotatedElementUtils.isAnnotated;

class OhRoot extends CommonRoot {

    Proxy clientProxy;
    SSLRoot sslRoot;
    Dispatcher dispatcher;
    ConnectionPool connectionPool;
    TimeoutRoot timeoutRoot;
    List<Interceptor> interceptors;
    Level loggingLevel;
    OkHttpClient okHttpClient;

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
            processSSLRootWithConfigurer(sslRoot, sslConfigurer, disabledSSLSocketFactory,
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
        if (configurer instanceof IsolatedDispatcherConfigurer dispatcherConfigurer) {
            return dispatcherConfigurer.isolatedDispatcher() ?
                    nullThen(dispatcherConfigurer.customDispatcher(), OhDispatcherElf::newDispatcher) : null;
        } else {
            return isAnnotated(element, IsolatedDispatcher.class) ? OhDispatcherElf.newDispatcher() : null;
        }
    }

    static ConnectionPool checkConnectionPool(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof IsolatedConnectionPoolConfigurer poolConfigurer) {
            return poolConfigurer.isolatedConnectionPool() ?
                    nullThen(poolConfigurer.customConnectionPool(), OhConnectionPoolElf::newConnectionPool) : null;
        } else {
            return isAnnotated(element, IsolatedConnectionPool.class) ? OhConnectionPoolElf.newConnectionPool() : null;
        }
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
