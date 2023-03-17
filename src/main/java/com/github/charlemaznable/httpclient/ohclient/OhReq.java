package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.common.CommonReq;
import com.github.charlemaznable.httpclient.common.FallbackFunction;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.ohclient.elf.OhConnectionPoolElf;
import com.github.charlemaznable.httpclient.ohclient.elf.OhDispatcherElf;
import com.github.charlemaznable.httpclient.ohclient.elf.SSLTrustAll;
import com.github.charlemaznable.httpclient.ohclient.internal.OhCallbackFuture;
import com.github.charlemaznable.httpclient.ohclient.internal.OhResponseBody;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.net.Proxy;
import java.util.List;
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
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.ACCEPT_CHARSET;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.CONTENT_TYPE;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_CALL_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_CONNECT_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_CONTENT_FORMATTER;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_READ_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_WRITE_TIMEOUT;
import static java.util.Objects.nonNull;

public class OhReq extends CommonReq<OhReq> {

    private static final Dispatcher globalDispatcher = OhDispatcherElf.newDispatcher();
    private static final ConnectionPool globalConnectionPool = OhConnectionPoolElf.newConnectionPool();
    private final List<Interceptor> interceptors;
    private final HttpLoggingInterceptor loggingInterceptor;
    private Proxy clientProxy;
    private SSLSocketFactory sslSocketFactory;
    private X509TrustManager x509TrustManager;
    private HostnameVerifier hostnameVerifier;
    private Dispatcher dispatcher;
    private ConnectionPool connectionPool;
    private long callTimeout = DEFAULT_CALL_TIMEOUT; // in milliseconds
    private long connectTimeout = DEFAULT_CONNECT_TIMEOUT; // in milliseconds
    private long readTimeout = DEFAULT_READ_TIMEOUT; // in milliseconds
    private long writeTimeout = DEFAULT_WRITE_TIMEOUT; // in milliseconds

    public OhReq() {
        super();
        this.interceptors = newArrayList();
        this.loggingInterceptor = new HttpLoggingInterceptor();
    }

    public OhReq(String baseUrl) {
        super(baseUrl);
        this.interceptors = newArrayList();
        this.loggingInterceptor = new HttpLoggingInterceptor();
    }

    public OhReq(OhReq other) {
        super(other);
        this.interceptors = newArrayList(other.interceptors);
        this.loggingInterceptor = new HttpLoggingInterceptor().setLevel(other.loggingInterceptor.getLevel());
        this.clientProxy = other.clientProxy;
        this.sslSocketFactory = other.sslSocketFactory;
        this.x509TrustManager = other.x509TrustManager;
        this.hostnameVerifier = other.hostnameVerifier;
        this.dispatcher = other.dispatcher;
        this.connectionPool = other.connectionPool;
        this.callTimeout = other.callTimeout;
        this.connectTimeout = other.connectTimeout;
        this.readTimeout = other.readTimeout;
        this.writeTimeout = other.writeTimeout;
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

    public OhReq dispatcher(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
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

    public String get() {
        return buildInstance().get();
    }

    public String post() {
        return buildInstance().post();
    }

    public Future<String> getFuture() {
        return buildInstance().getFuture();
    }

    public Future<String> postFuture() {
        return buildInstance().postFuture();
    }

    public OkHttpClient buildHttpClient() {
        val httpClientBuilder = new OkHttpClient.Builder().proxy(this.clientProxy);
        notNullThen(this.sslSocketFactory, xx -> checkNull(this.x509TrustManager,
                () -> httpClientBuilder.sslSocketFactory(this.sslSocketFactory, SSLTrustAll.x509TrustManager()),
                yy -> httpClientBuilder.sslSocketFactory(this.sslSocketFactory, this.x509TrustManager)));
        notNullThen(this.hostnameVerifier, httpClientBuilder::hostnameVerifier);
        httpClientBuilder.dispatcher(nullThen(this.dispatcher, () -> globalDispatcher));
        httpClientBuilder.connectionPool(nullThen(this.connectionPool, () -> globalConnectionPool));
        httpClientBuilder.callTimeout(this.callTimeout, TimeUnit.MILLISECONDS);
        httpClientBuilder.connectTimeout(this.connectTimeout, TimeUnit.MILLISECONDS);
        httpClientBuilder.readTimeout(this.readTimeout, TimeUnit.MILLISECONDS);
        httpClientBuilder.writeTimeout(this.writeTimeout, TimeUnit.MILLISECONDS);
        this.interceptors.forEach(httpClientBuilder::addInterceptor);
        httpClientBuilder.addInterceptor(this.loggingInterceptor);
        return httpClientBuilder.build();
    }

    public Instance buildInstance() {
        return new Instance(buildHttpClient(), new OhReq(this));
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Instance {

        private final OkHttpClient httpClient;
        private final OhReq ohReq;

        public String get() {
            return this.execute(ohReq.buildGetRequest());
        }

        public String post() {
            return this.execute(ohReq.buildPostRequest());
        }

        public Future<String> getFuture() {
            return this.enqueue(ohReq.buildGetRequest());
        }

        public Future<String> postFuture() {
            return this.enqueue(ohReq.buildPostRequest());
        }

        @SneakyThrows
        private String execute(Request request) {
            return ohReq.processResponse(httpClient.newCall(request).execute());
        }

        private Future<String> enqueue(Request request) {
            val future = new OhCallbackFuture<>(ohReq::processResponse);
            httpClient.newCall(request).enqueue(future);
            return future;
        }
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
}
