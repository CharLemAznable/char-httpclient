package com.github.charlemaznable.httpclient.ohclient.internal;

import com.github.charlemaznable.httpclient.ohclient.annotation.ClientTimeout;
import lombok.val;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.internal.http.HttpMethod;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringSubstitutor;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.net.Proxy;

import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThenRun;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.toMap;
import static com.github.charlemaznable.core.net.Url.concatUrlQuery;
import static com.github.charlemaznable.httpclient.internal.CommonConstant.ACCEPT_CHARSET;
import static com.github.charlemaznable.httpclient.internal.CommonConstant.CONTENT_TYPE;
import static com.github.charlemaznable.httpclient.internal.CommonConstant.DEFAULT_CONTENT_FORMATTER;

@SuppressWarnings("rawtypes")
public final class OhCall extends OhRoot {

    Request request;

    OhCall(OhMappingProxy proxy, Object[] args) {
        initial(proxy);
        processArguments(proxy.ohMethod, args,
                this::processOkHttpParameter);
        this.okHttpClient = buildOkHttpClient(proxy);
        this.request = buildRequest(proxy);
    }

    Call newCall() {
        return this.okHttpClient.newCall(this.request);
    }

    private void initial(OhMappingProxy proxy) {
        this.clientProxy = proxy.clientProxy;
        this.sslRoot = new SSLRoot();
        this.sslRoot.sslSocketFactory = proxy.sslRoot.sslSocketFactory;
        this.sslRoot.x509TrustManager = proxy.sslRoot.x509TrustManager;
        this.sslRoot.hostnameVerifier = proxy.sslRoot.hostnameVerifier;
        this.dispatcher = proxy.dispatcher;
        this.connectionPool = proxy.connectionPool;
        this.timeoutRoot = new TimeoutRoot();
        this.timeoutRoot.callTimeout = proxy.timeoutRoot.callTimeout;
        this.timeoutRoot.connectTimeout = proxy.timeoutRoot.connectTimeout;
        this.timeoutRoot.readTimeout = proxy.timeoutRoot.readTimeout;
        this.timeoutRoot.writeTimeout = proxy.timeoutRoot.writeTimeout;
        this.interceptors = newArrayList(proxy.interceptors);
        this.loggingLevel = proxy.loggingLevel;

        this.headers = newArrayList(proxy.headers());
        this.pathVars = newArrayList(proxy.pathVars());
        this.parameters = newArrayList(proxy.parameters());
        this.contexts = newArrayList(proxy.contexts());
    }

    private boolean processOkHttpParameter(Object argument, Class parameterType) {
        if (Proxy.class.isAssignableFrom(parameterType)) {
            this.clientProxy = (Proxy) argument;
        } else if (SSLSocketFactory.class.isAssignableFrom(parameterType)) {
            this.sslRoot.sslSocketFactory = (SSLSocketFactory) argument;
        } else if (X509TrustManager.class.isAssignableFrom(parameterType)) {
            this.sslRoot.x509TrustManager = (X509TrustManager) argument;
        } else if (HostnameVerifier.class.isAssignableFrom(parameterType)) {
            this.sslRoot.hostnameVerifier = (HostnameVerifier) argument;
        } else if (argument instanceof ClientTimeout clientTimeout) {
            this.timeoutRoot.callTimeout = clientTimeout.callTimeout();
            this.timeoutRoot.connectTimeout = clientTimeout.connectTimeout();
            this.timeoutRoot.readTimeout = clientTimeout.readTimeout();
            this.timeoutRoot.writeTimeout = clientTimeout.writeTimeout();
        } else if (argument instanceof Interceptor interceptor) {
            this.interceptors.add(interceptor);
        } else if (argument instanceof Level level) {
            this.loggingLevel = level;
        } else {
            return false;
        }
        return true;
    }

    private OkHttpClient buildOkHttpClient(OhMappingProxy proxy) {
        val sameClientProxy = this.clientProxy == proxy.clientProxy;
        val sameSSLSocketFactory = this.sslRoot.sslSocketFactory == proxy.sslRoot.sslSocketFactory;
        val sameX509TrustManager = this.sslRoot.x509TrustManager == proxy.sslRoot.x509TrustManager;
        val sameHostnameVerifier = this.sslRoot.hostnameVerifier == proxy.sslRoot.hostnameVerifier;
        val sameCallTimeout = this.timeoutRoot.callTimeout == proxy.timeoutRoot.callTimeout;
        val sameConnectTimeout = this.timeoutRoot.connectTimeout == proxy.timeoutRoot.connectTimeout;
        val sameReadTimeout = this.timeoutRoot.readTimeout == proxy.timeoutRoot.readTimeout;
        val sameWriteTimeout = this.timeoutRoot.writeTimeout == proxy.timeoutRoot.writeTimeout;
        val sameInterceptors = this.interceptors.equals(proxy.interceptors);
        val sameLoggingLevel = this.loggingLevel == proxy.loggingLevel;
        if (sameClientProxy && sameSSLSocketFactory && sameX509TrustManager && sameHostnameVerifier
                && sameCallTimeout && sameConnectTimeout && sameReadTimeout && sameWriteTimeout
                && sameInterceptors && sameLoggingLevel) return proxy.okHttpClient;

        return OhRoot.buildOkHttpClient(this);
    }

    @SuppressWarnings("DuplicatedCode")
    private Request buildRequest(OhMappingProxy proxy) {
        val requestBuilder = new Request.Builder();

        notNullThenRun(proxy.requestExtender(), extender -> extender.extend(
                this.headers, this.pathVars, this.parameters, this.contexts));

        val headersBuilder = new Headers.Builder();
        val acceptCharsetName = proxy.acceptCharset().name();
        headersBuilder.set(ACCEPT_CHARSET, acceptCharsetName);
        val contentType = proxy.contentFormatter().contentType();
        headersBuilder.set(CONTENT_TYPE, contentType);
        for (val header : this.headers) {
            checkNull(header.getValue(),
                    () -> headersBuilder.removeAll(header.getKey()),
                    xx -> headersBuilder.set(header.getKey(), header.getValue()));
        }
        requestBuilder.headers(headersBuilder.build());

        val pathVarMap = this.pathVars.stream().collect(toMap(Pair::getKey, Pair::getValue));
        val parameterMap = this.parameters.stream().collect(toMap(Pair::getKey, Pair::getValue));
        val contextMap = this.contexts.stream().collect(toMap(Pair::getKey, Pair::getValue));

        val pathVarSubstitutor = new StringSubstitutor(pathVarMap, "{", "}");
        val substitutedUrl = pathVarSubstitutor.replace(
                proxy.mappingBalancer().choose(proxy.requestUrls));
        val extraUrlQuery = checkNull(proxy.extraUrlQueryBuilder(),
                () -> "", builder -> builder.build(parameterMap, contextMap));
        val requestUrl = concatUrlQuery(substitutedUrl, extraUrlQuery);

        val requestMethod = proxy.httpMethod().toString();
        if (!HttpMethod.permitsRequestBody(requestMethod)) {
            requestBuilder.method(requestMethod, null);
            val query = URL_QUERY_FORMATTER.format(parameterMap, contextMap);
            requestBuilder.url(concatUrlQuery(requestUrl, query));

        } else {
            val content = nullThen(this.requestBodyRaw, () ->
                    proxy.contentFormatter().format(parameterMap, contextMap));
            val contentTypeHeader = nullThen(headersBuilder.get(CONTENT_TYPE),
                    DEFAULT_CONTENT_FORMATTER::contentType);
            requestBuilder.method(requestMethod, RequestBody.create(
                    content, MediaType.parse(contentTypeHeader)));
            requestBuilder.url(requestUrl);
        }
        return requestBuilder.build();
    }
}
