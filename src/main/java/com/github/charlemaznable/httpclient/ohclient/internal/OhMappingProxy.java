package com.github.charlemaznable.httpclient.ohclient.internal;

import com.github.charlemaznable.configservice.ConfigListener;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.httpclient.configurer.Configurer;
import com.github.charlemaznable.httpclient.internal.ResponseProcessor;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientInterceptorCleanup;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientProxy;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientSSL;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientInterceptorsCleanupConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientProxyDisabledConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientSSLDisabledConfigurer;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.toMap;
import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.core.annotation.AnnotatedElementUtils.isAnnotated;

@SuppressWarnings("rawtypes")
public final class OhMappingProxy extends OhRoot implements Reloadable {

    private static final String RETURN_GENERIC_ERROR = "Method return type generic Error";

    Class ohClass;
    Method ohMethod;
    Factory factory;
    OhProxy proxy;
    ConfigListener configListener;
    Configurer configurer;
    List<String> requestUrls;

    OhResponseProcessor responseProcessor;

    OhMappingProxy(Class ohClass, Method ohMethod,
                   Factory factory, OhProxy proxy) {
        this.ohClass = ohClass;
        this.ohMethod = ohMethod;
        this.factory = factory;
        this.proxy = proxy;
        this.configListener = (keyset, key, value) -> reload();
        this.initialize();
        this.processReturnType(this.ohMethod);
    }

    @SneakyThrows
    Object execute(Object[] args) {
        val ohCall = new OhCall(this, args);
        val call = ohCall.newCall();
        val responseClass = ohCall.responseClass();
        val contexts = ohCall.contexts();

        if (this.responseProcessor.isReturnFuture()) {
            val future = new OhCallbackFuture<>(response ->
                    this.responseProcessor.processResponse(
                            response, responseClass, contexts));
            call.enqueue(future);
            return future;
        }
        return this.responseProcessor.processResponse(
                call.execute(), responseClass, contexts);
    }

    @Override
    public void reload() {
        this.initialize();
    }

    private void initialize() {
        checkConfigurerIsRegisterThenRun(this.configurer, register ->
                register.removeConfigListener(this.configListener));
        this.configurer = checkConfigurer(this.ohMethod, this.factory);
        checkConfigurerIsRegisterThenRun(this.configurer, register ->
                register.addConfigListener(this.configListener));

        setUpBeforeInitialization(this.configurer, this.ohMethod, this.ohClass, this.proxy.configurer);

        this.requestUrls = checkRequestUrls(this.configurer, this.ohMethod,
                OhDummy::substitute, this.proxy.baseUrls, this.proxy.mappingMethodNameDisabled);

        this.clientProxy = Elf.checkClientProxy(this.configurer, this.ohMethod, this.proxy);
        this.sslRoot = Elf.checkClientSSL(this.configurer, this.ohMethod, this.factory, this.proxy);
        this.dispatcher = nullThen(checkDispatcher(
                this.configurer, this.ohMethod), () -> this.proxy.dispatcher);
        this.connectionPool = nullThen(checkConnectionPool(
                this.configurer, this.ohMethod), () -> this.proxy.connectionPool);
        this.timeoutRoot = Elf.checkClientTimeout(this.configurer, this.ohMethod, this.proxy);
        this.interceptors = Elf.defaultClientInterceptors(this.configurer, this.ohMethod, this.proxy);
        this.interceptors.addAll(checkClientInterceptors(this.configurer, this.ohMethod, this.factory));
        this.loggingLevel = nullThen(checkClientLoggingLevel(
                this.configurer, this.ohMethod), () -> this.proxy.loggingLevel);
        this.okHttpClient = Elf.buildOkHttpClient(this, this.proxy);

        initialize(this, this.factory, this.configurer, this.ohMethod, this.proxy);

        tearDownAfterInitialization(this.configurer, this.ohMethod, this.ohClass, this.proxy.configurer);
    }

    private void processReturnType(Method method) {
        this.responseProcessor = new OhResponseProcessor();
        this.responseProcessor.setFactory(this.factory);
        this.responseProcessor.setRoot(this);
        this.responseProcessor.processReturnType(method, Future.class);
    }

    static class OhResponseProcessor extends ResponseProcessor<Response, ResponseBody> {

        @Override
        protected int getResponseCode(Response response) {
            return response.code();
        }

        @Override
        protected ResponseBody getResponseBody(Response response) {
            val responseBody = notNullThen(response.body(), OhResponseBody::new);
            if (nonNull(response.body())) response.close();
            return responseBody;
        }

        @Override
        protected String responseBodyToString(ResponseBody responseBody) {
            return ResponseBodyExtractor.string(responseBody);
        }

        @Override
        protected Object customProcessReturnTypeValue(int statusCode,
                                                      ResponseBody responseBody,
                                                      Class returnType,
                                                      List<Pair<String, Object>> contexts) {
            if (ResponseBody.class.isAssignableFrom(returnType)) {
                return responseBody;
            } else if (InputStream.class == returnType) {
                return notNullThen(responseBody, ResponseBodyExtractor::byteStream);
            } else if (BufferedSource.class.isAssignableFrom(returnType)) {
                return (notNullThen(responseBody, ResponseBodyExtractor::source));
            } else if (byte[].class == returnType) {
                return notNullThen(responseBody, ResponseBodyExtractor::bytes);
            } else if (Reader.class.isAssignableFrom(returnType)) {
                return notNullThen(responseBody, ResponseBodyExtractor::charStream);
            } else if (returnUnCollectionString(returnType)) {
                return notNullThen(responseBody, ResponseBodyExtractor::string);
            } else {
                return notNullThen(responseBody, body ->
                        ResponseBodyExtractor.object(body, notNullThen(getRoot().responseParser(), parser -> {
                            val contextMap = contexts.stream().collect(toMap(Pair::getKey, Pair::getValue));
                            return content -> parser.parse(content, returnType, contextMap);
                        }), returnType));
            }
        }
    }

    @NoArgsConstructor(access = PRIVATE)
    static class Elf {

        static Proxy checkClientProxy(Configurer configurer, Method method, OhProxy proxy) {
            if (configurer instanceof ClientProxyDisabledConfigurer disabledConfigurer
                    ? disabledConfigurer.disabledClientProxy()
                    : isAnnotated(method, ClientProxy.Disabled.class)) return null;
            return nullThen(OhRoot.checkClientProxy(configurer, method), () -> proxy.clientProxy);
        }

        static SSLRoot checkClientSSL(Configurer configurer, Method method, Factory factory, OhProxy proxy) {
            boolean disabledSSLSocketFactory;
            boolean disabledX509TrustManager;
            boolean disabledHostnameVerifier;
            if (configurer instanceof ClientSSLDisabledConfigurer disabledConfigurer) {
                disabledSSLSocketFactory = disabledConfigurer.disabledSSLSocketFactory();
                disabledX509TrustManager = disabledConfigurer.disabledX509TrustManager();
                disabledHostnameVerifier = disabledConfigurer.disabledHostnameVerifier();
            } else {
                disabledSSLSocketFactory = isAnnotated(method, ClientSSL.DisabledSSLSocketFactory.class);
                disabledX509TrustManager = isAnnotated(method, ClientSSL.DisabledX509TrustManager.class);
                disabledHostnameVerifier = isAnnotated(method, ClientSSL.DisabledHostnameVerifier.class);
            }
            return OhRoot.checkClientSSL(configurer, method, factory,
                    disabledSSLSocketFactory, disabledX509TrustManager, disabledHostnameVerifier, proxy.sslRoot);
        }

        static TimeoutRoot checkClientTimeout(Configurer configurer, Method method, OhProxy proxy) {
            return OhRoot.checkClientTimeout(configurer, method, proxy.timeoutRoot);
        }

        static List<Interceptor> defaultClientInterceptors(Configurer configurer, Method method, OhProxy proxy) {
            val cleanup = configurer instanceof ClientInterceptorsCleanupConfigurer cleanupConfigurer
                    ? cleanupConfigurer.cleanupInterceptors() : isAnnotated(method, ClientInterceptorCleanup.class);
            return newArrayList(cleanup ? null : proxy.interceptors);
        }

        static OkHttpClient buildOkHttpClient(OhMappingProxy mappingProxy, OhProxy proxy) {
            val sameClientProxy = mappingProxy.clientProxy == proxy.clientProxy;
            val sameSSLSocketFactory = mappingProxy.sslRoot.sslSocketFactory == proxy.sslRoot.sslSocketFactory;
            val sameX509TrustManager = mappingProxy.sslRoot.x509TrustManager == proxy.sslRoot.x509TrustManager;
            val sameHostnameVerifier = mappingProxy.sslRoot.hostnameVerifier == proxy.sslRoot.hostnameVerifier;
            val sameDispatcher = mappingProxy.dispatcher == proxy.dispatcher;
            val sameConnectionPool = mappingProxy.connectionPool == proxy.connectionPool;
            val sameCallTimeout = mappingProxy.timeoutRoot.callTimeout == proxy.timeoutRoot.callTimeout;
            val sameConnectTimeout = mappingProxy.timeoutRoot.connectTimeout == proxy.timeoutRoot.connectTimeout;
            val sameReadTimeout = mappingProxy.timeoutRoot.readTimeout == proxy.timeoutRoot.readTimeout;
            val sameWriteTimeout = mappingProxy.timeoutRoot.writeTimeout == proxy.timeoutRoot.writeTimeout;
            val sameInterceptors = mappingProxy.interceptors.equals(proxy.interceptors);
            val sameLoggingLevel = mappingProxy.loggingLevel == proxy.loggingLevel;
            if (sameClientProxy && sameSSLSocketFactory && sameX509TrustManager
                    && sameHostnameVerifier && sameConnectionPool && sameDispatcher
                    && sameCallTimeout && sameConnectTimeout && sameReadTimeout && sameWriteTimeout
                    && sameInterceptors && sameLoggingLevel) return proxy.okHttpClient;

            return OhRoot.buildOkHttpClient(mappingProxy);
        }
    }
}
