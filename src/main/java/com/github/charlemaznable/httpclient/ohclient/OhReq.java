package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.common.CommonReq;
import com.github.charlemaznable.httpclient.common.FallbackFunction;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.ohclient.elf.SSLTrustAll;
import com.github.charlemaznable.httpclient.ohclient.internal.OhResponseBody;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static com.github.charlemaznable.core.net.Url.concatUrlQuery;
import static com.github.charlemaznable.httpclient.ohclient.elf.OhExecutorServiceBuilderElf.buildExecutorService;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.ACCEPT_CHARSET;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.CONTENT_TYPE;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_CALL_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_CONNECT_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_CONTENT_FORMATTER;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_MAX_REQUESTS;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_MAX_REQUESTS_PER_HOST;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_READ_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_WRITE_TIMEOUT;
import static java.util.Objects.nonNull;

public class OhReq extends CommonReq<OhReq> {

    private static final ConnectionPool globalConnectionPool = new ConnectionPool();
    private final List<Interceptor> interceptors = newArrayList();
    private final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
    private Proxy clientProxy;
    private SSLSocketFactory sslSocketFactory;
    private X509TrustManager x509TrustManager;
    private HostnameVerifier hostnameVerifier;
    private ConnectionPool connectionPool;
    private long callTimeout = DEFAULT_CALL_TIMEOUT; // in milliseconds
    private long connectTimeout = DEFAULT_CONNECT_TIMEOUT; // in milliseconds
    private long readTimeout = DEFAULT_READ_TIMEOUT; // in milliseconds
    private long writeTimeout = DEFAULT_WRITE_TIMEOUT; // in milliseconds
    private int maxRequests = DEFAULT_MAX_REQUESTS;
    private int maxRequestsPerHost = DEFAULT_MAX_REQUESTS_PER_HOST;

    public OhReq() {
        super();
    }

    public OhReq(String baseUrl) {
        super(baseUrl);
    }

    public OhReq clientProxy(Proxy proxy) {
        this.clientProxy = proxy;
        return this;
    }

    public OhReq sslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        return this;
    }

    public OhReq x509TrustManager(X509TrustManager x509TrustManager) {
        this.x509TrustManager = x509TrustManager;
        return this;
    }

    public OhReq hostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }

    public OhReq connectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
        return this;
    }

    public OhReq callTimeout(long callTimeout) {
        this.callTimeout = callTimeout;
        return this;
    }

    public OhReq connectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public OhReq readTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public OhReq writeTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    public OhReq addInterceptor(Interceptor interceptor) {
        if (nonNull(interceptor)) {
            this.interceptors.add(interceptor);
        }
        return this;
    }

    public OhReq addInterceptors(Iterable<Interceptor> interceptors) {
        interceptors.forEach(this::addInterceptor);
        return this;
    }

    public OhReq loggingLevel(Level level) {
        this.loggingInterceptor.setLevel(level);
        return this;
    }

    public OhReq maxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
        return this;
    }

    public OhReq maxRequestsPerHost(int maxRequestsPerHost) {
        this.maxRequestsPerHost = maxRequestsPerHost;
        return this;
    }

    public String get() {
        return this.execute(buildGetRequest());
    }

    public String post() {
        return this.execute(buildPostRequest());
    }

    public Future<String> getFuture() {
        return this.enqueue(buildGetRequest());
    }

    public Future<String> postFuture() {
        return this.enqueue(buildPostRequest());
    }

    public OkHttpClient buildHttpClient() {
        val httpClientBuilder = new OkHttpClient.Builder().proxy(this.clientProxy);
        notNullThen(this.sslSocketFactory, xx -> checkNull(this.x509TrustManager,
                () -> httpClientBuilder.sslSocketFactory(this.sslSocketFactory, SSLTrustAll.x509TrustManager()),
                yy -> httpClientBuilder.sslSocketFactory(this.sslSocketFactory, this.x509TrustManager)));
        notNullThen(this.hostnameVerifier, httpClientBuilder::hostnameVerifier);
        httpClientBuilder.connectionPool(nullThen(this.connectionPool, () -> globalConnectionPool));
        httpClientBuilder.callTimeout(this.callTimeout, TimeUnit.MILLISECONDS);
        httpClientBuilder.connectTimeout(this.connectTimeout, TimeUnit.MILLISECONDS);
        httpClientBuilder.readTimeout(this.readTimeout, TimeUnit.MILLISECONDS);
        httpClientBuilder.writeTimeout(this.writeTimeout, TimeUnit.MILLISECONDS);
        this.interceptors.forEach(httpClientBuilder::addInterceptor);
        httpClientBuilder.addInterceptor(this.loggingInterceptor);
        val httpClient = httpClientBuilder.build();
        httpClient.dispatcher().setMaxRequests(this.maxRequests);
        httpClient.dispatcher().setMaxRequestsPerHost(this.maxRequestsPerHost);
        return httpClient;
    }

    private Request buildGetRequest() {
        val parameterMap = fetchParameterMap();
        val requestUrl = concatRequestUrl(parameterMap);
        val headersBuilder = buildHeadersBuilder();
        val requestBuilder = new Request.Builder();
        requestBuilder.headers(headersBuilder.build());

        requestBuilder.method(HttpMethod.GET.toString(), null);
        val query = URL_QUERY_FORMATTER.format(parameterMap, newHashMap());
        requestBuilder.url(concatUrlQuery(requestUrl, query));
        return requestBuilder.build();
    }

    private Request buildPostRequest() {
        val parameterMap = fetchParameterMap();
        val requestUrl = concatRequestUrl(parameterMap);
        val headersBuilder = buildHeadersBuilder();
        val requestBuilder = new Request.Builder();
        requestBuilder.headers(headersBuilder.build());

        val content = nullThen(this.requestBody, () ->
                this.contentFormatter.format(parameterMap, newHashMap()));
        val contentType = nullThen(headersBuilder.get(CONTENT_TYPE),
                DEFAULT_CONTENT_FORMATTER::contentType);
        requestBuilder.method(HttpMethod.POST.toString(),
                RequestBody.create(content, MediaType.parse(contentType)));
        requestBuilder.url(requestUrl);
        return requestBuilder.build();
    }

    @SuppressWarnings("DuplicatedCode")
    private Headers.Builder buildHeadersBuilder() {
        val headersBuilder = new Headers.Builder();
        val acceptCharsetName = this.acceptCharset.name();
        headersBuilder.set(ACCEPT_CHARSET, acceptCharsetName);
        val contentType = this.contentFormatter.contentType();
        headersBuilder.set(CONTENT_TYPE, contentType);
        for (val header : this.headers) {
            checkNull(header.getValue(),
                    () -> headersBuilder.removeAll(header.getKey()),
                    xx -> headersBuilder.set(header.getKey(), header.getValue()));
        }
        return headersBuilder;
    }

    @SneakyThrows
    private String execute(Request request) {
        return processResponse(buildHttpClient().newCall(request).execute());
    }

    private Future<String> enqueue(Request request) {
        val future = new CallbackFuture(this);
        buildHttpClient().newCall(request).enqueue(future);
        return future;
    }

    private String processResponse(Response response) {
        val statusCode = response.code();
        val responseBody = notNullThen(response.body(), OhResponseBody::new);
        if (nonNull(response.body())) response.close();

        val statusFallback = statusFallbackMapping
                .get(HttpStatus.valueOf(statusCode));
        if (nonNull(statusFallback)) {
            return applyFallback(statusFallback,
                    statusCode, responseBody);
        }

        val statusSeriesFallback = statusSeriesFallbackMapping
                .get(HttpStatus.Series.valueOf(statusCode));
        if (nonNull(statusSeriesFallback)) {
            return applyFallback(statusSeriesFallback,
                    statusCode, responseBody);
        }

        return notNullThen(responseBody, OhReq::extractResponseString);
    }

    @SuppressWarnings("rawtypes")
    private String applyFallback(Class<? extends FallbackFunction> function,
                                 int statusCode, ResponseBody responseBody) {
        return toStr(reflectFactory().build(function).apply(
                new FallbackFunction.Response<>(statusCode, responseBody) {
                    @Override
                    public String responseBodyAsString() {
                        return toStr(notNullThen(getResponseBody(),
                                OhReq::extractResponseString));
                    }
                }));
    }

    @SneakyThrows
    private static String extractResponseString(ResponseBody responseBody) {
        return responseBody.string();
    }

    @AllArgsConstructor
    private static class CallbackFuture extends CompletableFuture<String> implements Callback {

        @Nonnull
        private OhReq ohReq;

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            super.completeExceptionally(e);
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) {
            try {
                super.complete(ohReq.processResponse(response));
            } catch (Exception e) {
                super.completeExceptionally(e);
            }
        }
    }
}
