package com.github.charlemaznable.httpclient.ohclient.internal;

import com.github.charlemaznable.core.lang.Reflectt;
import com.github.charlemaznable.core.lang.Str;
import com.github.charlemaznable.httpclient.common.Bundle;
import com.github.charlemaznable.httpclient.common.CncRequest;
import com.github.charlemaznable.httpclient.common.CncResponse;
import com.github.charlemaznable.httpclient.common.ContentFormat;
import com.github.charlemaznable.httpclient.common.Context;
import com.github.charlemaznable.httpclient.common.Header;
import com.github.charlemaznable.httpclient.common.Parameter;
import com.github.charlemaznable.httpclient.common.PathVar;
import com.github.charlemaznable.httpclient.common.RequestBodyRaw;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientTimeout;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
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
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.net.Proxy;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.charlemaznable.core.codec.Json.desc;
import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.notNullThenRun;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.toMap;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static com.github.charlemaznable.core.net.Url.concatUrlQuery;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.ACCEPT_CHARSET;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.CONTENT_TYPE;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_CONTENT_FORMATTER;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhDummy.log;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.joor.Reflect.on;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

@SuppressWarnings("rawtypes")
public final class OhCall extends OhRoot {

    private static final ContentFormat.ContentFormatter URL_QUERY_FORMATTER = new ContentFormat.FormContentFormatter();

    Class responseClass = CncResponse.CncResponseImpl.class;
    String requestBodyRaw;
    Request request;

    OhCall(OhMappingProxy proxy, Object[] args) {
        initial(proxy);
        processArguments(proxy.ohMethod, args);
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

    private void processArguments(Method method, Object[] args) {
        val parameterTypes = method.getParameterTypes();
        val parameters = method.getParameters();
        for (int i = 0; i < args.length; i++) {
            val argument = args[i];
            val parameterType = parameterTypes[i];
            val parameter = parameters[i];

            val configuredType = processParameterType(argument, parameterType);
            if (configuredType) continue;
            processAnnotations(argument, parameter, null);
        }
    }

    private boolean processParameterType(Object argument, Class parameterType) {
        if (Proxy.class.isAssignableFrom(parameterType)) {
            this.clientProxy = (Proxy) argument;
        } else if (SSLSocketFactory.class.isAssignableFrom(parameterType)) {
            this.sslRoot.sslSocketFactory = (SSLSocketFactory) argument;
        } else if (X509TrustManager.class.isAssignableFrom(parameterType)) {
            this.sslRoot.x509TrustManager = (X509TrustManager) argument;
        } else if (HostnameVerifier.class.isAssignableFrom(parameterType)) {
            this.sslRoot.hostnameVerifier = (HostnameVerifier) argument;
        } else if (ClientTimeout.class.isAssignableFrom(parameterType)) {
            if (nonNull(argument)) {
                ClientTimeout clientTimeout = (ClientTimeout) argument;
                this.timeoutRoot.callTimeout = clientTimeout.callTimeout();
                this.timeoutRoot.connectTimeout = clientTimeout.connectTimeout();
                this.timeoutRoot.readTimeout = clientTimeout.readTimeout();
                this.timeoutRoot.writeTimeout = clientTimeout.writeTimeout();
            }
        } else if (CncRequest.class.isAssignableFrom(parameterType)) {
            this.responseClass = checkNull(argument,
                    () -> CncResponse.CncResponseImpl.class,
                    xx -> ((CncRequest) xx).responseClass());
            return false;
        } else if (Interceptor.class.isAssignableFrom(parameterType)) {
            this.interceptors.add((Interceptor) argument);
        } else if (argument instanceof Level) {
            this.loggingLevel = (Level) argument;
        } else {
            return false;
        }
        return true;
    }

    private void processAnnotations(Object argument,
                                    AnnotatedElement annotatedElement,
                                    String defaultParameterName) {
        final AtomicBoolean processed = new AtomicBoolean(false);
        notNullThenRun(findAnnotation(annotatedElement, Header.class), header -> {
            processHeader(argument, header);
            processed.set(true);
        });
        notNullThenRun(findAnnotation(annotatedElement, PathVar.class), pathVar -> {
            processPathVar(argument, pathVar);
            processed.set(true);
        });
        notNullThenRun(findAnnotation(annotatedElement, Parameter.class), parameter -> {
            processParameter(argument, parameter);
            processed.set(true);
        });
        notNullThenRun(findAnnotation(annotatedElement, Context.class), context -> {
            processContext(argument, context);
            processed.set(true);
        });
        notNullThenRun(findAnnotation(annotatedElement, RequestBodyRaw.class), xx -> {
            processRequestBodyRaw(argument);
            processed.set(true);
        });
        notNullThenRun(findAnnotation(annotatedElement, Bundle.class), xx -> {
            processBundle(argument);
            processed.set(true);
        });
        if (!processed.get() && nonNull(defaultParameterName)) {
            processParameter(argument, new ParameterImpl(defaultParameterName));
        }
    }

    private void processHeader(Object argument, Header header) {
        this.headers.add(Pair.of(header.value(),
                notNullThen(argument, Str::toStr)));
    }

    private void processPathVar(Object argument, PathVar pathVar) {
        this.pathVars.add(Pair.of(pathVar.value(),
                notNullThen(argument, Str::toStr)));
    }

    private void processParameter(Object argument, Parameter parameter) {
        this.parameters.add(Pair.of(parameter.value(), argument));
    }

    private void processContext(Object argument, Context context) {
        this.contexts.add(Pair.of(context.value(), argument));
    }

    private void processRequestBodyRaw(Object argument) {
        if (null == argument || (argument instanceof String)) {
            // OhRequestBodyRaw参数传值null时, 则继续使用parameters构造请求
            this.requestBodyRaw = (String) argument;
            return;
        }
        log.warn("Argument annotated with @RequestBodyRaw, " +
                "but Type is {} instead String.", argument.getClass());
    }

    private void processBundle(Object argument) {
        if (isNull(argument)) return;
        if (argument instanceof Map) {
            desc(argument).forEach((key, value) ->
                    processParameter(value, new ParameterImpl(toStr(key))));
            return;
        }

        val clazz = argument.getClass();
        val reflect = on(argument);
        val fields = reflect.fields();
        for (val fieldEntry : fields.entrySet()) {
            val fieldName = fieldEntry.getKey();
            processAnnotations(fieldEntry.getValue().get(),
                    Reflectt.field0(clazz, fieldName), fieldName);
        }
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

    @SuppressWarnings("ClassExplicitlyAnnotation")
    @AllArgsConstructor
    private static class ParameterImpl implements Parameter {

        @Getter
        @Accessors(fluent = true)
        private final Class<? extends Annotation> annotationType = Parameter.class;
        private String value;

        @Override
        public String value() {
            return value;
        }
    }
}
