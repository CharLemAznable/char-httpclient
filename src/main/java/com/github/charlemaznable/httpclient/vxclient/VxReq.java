package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.httpclient.common.CommonReq;
import com.github.charlemaznable.httpclient.common.ContentFormat;
import com.github.charlemaznable.httpclient.common.ExtraUrlQuery;
import com.github.charlemaznable.httpclient.common.FallbackFunction;
import com.github.charlemaznable.httpclient.common.FallbackFunction.Response;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.KeyCertOptions;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.TrustOptions;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.val;
import okhttp3.MediaType;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThenRun;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static com.github.charlemaznable.core.net.Url.concatUrlQuery;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.ACCEPT_CHARSET;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.CONTENT_TYPE;
import static com.google.common.collect.Iterators.forArray;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

public class VxReq extends CommonReq<VxReq> {

    private final Vertx vertx;
    private final WebClientOptions webClientOptions;

    public VxReq(Vertx vertx) {
        super();
        this.vertx = vertx;
        this.webClientOptions = new WebClientOptions();
    }

    public VxReq(Vertx vertx, String baseUrl) {
        super(baseUrl);
        this.vertx = vertx;
        this.webClientOptions = new WebClientOptions();
    }

    public VxReq(CommonReq<?> other) {
        super(other);
        this.vertx = null;
        this.webClientOptions = new WebClientOptions();
    }

    public VxReq(VxReq other) {
        super(other);
        this.vertx = other.vertx;
        this.webClientOptions = new WebClientOptions(other.webClientOptions);
    }

    public VxReq proxyOptions(ProxyOptions proxyOptions) {
        webClientOptions.setProxyOptions(proxyOptions);
        return this;
    }

    public VxReq keyCertOptions(KeyCertOptions keyCertOptions) {
        webClientOptions.setKeyCertOptions(keyCertOptions);
        return this;
    }

    public VxReq trustOptions(TrustOptions trustOptions) {
        webClientOptions.setTrustOptions(trustOptions);
        return this;
    }

    public VxReq verifyHost(boolean verifyHost) {
        webClientOptions.setVerifyHost(verifyHost);
        return this;
    }

    public VxReq connectTimeout(int connectTimeout) {
        webClientOptions.setConnectTimeout(connectTimeout);
        return this;
    }

    @SafeVarargs
    public final void get(Handler<AsyncResult<String>>... handlers) {
        buildInstance().get(handlers);
    }

    @SafeVarargs
    public final void post(Handler<AsyncResult<String>>... handlers) {
        buildInstance().post(handlers);
    }

    public Future<String> get() {
        return buildInstance().get();
    }

    public Future<String> post() {
        return buildInstance().post();
    }

    public WebClient buildWebClient() {
        return WebClient.create(requireNonNull(vertx), webClientOptions);
    }

    public VxReq.Instance buildInstance() {
        return new Instance(buildWebClient(), new CommonReq.Instance(this));
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Instance extends CommonReq<Instance> {

        private final WebClient webClient;
        private final CommonReq.Instance req;

        @Override
        public VxReq.Instance req(String reqPath) {
            return new VxReq.Instance(this.webClient,
                    this.req.copy().req(reqPath));
        }

        @Override
        public VxReq.Instance acceptCharset(Charset acceptCharset) {
            return new VxReq.Instance(this.webClient,
                    this.req.copy().acceptCharset(acceptCharset));
        }

        @Override
        public VxReq.Instance contentFormat(ContentFormat.ContentFormatter contentFormatter) {
            return new VxReq.Instance(this.webClient,
                    this.req.copy().contentFormat(contentFormatter));
        }

        @Override
        public VxReq.Instance header(String name, String value) {
            return new VxReq.Instance(this.webClient,
                    this.req.copy().header(name, value));
        }

        @Override
        public VxReq.Instance headers(Map<String, String> headers) {
            return new VxReq.Instance(this.webClient,
                    this.req.copy().headers(headers));
        }

        @Override
        public VxReq.Instance parameter(String name, Object value) {
            return new VxReq.Instance(this.webClient,
                    this.req.copy().parameter(name, value));
        }

        @Override
        public VxReq.Instance parameters(Map<String, Object> parameters) {
            return new VxReq.Instance(this.webClient,
                    this.req.copy().parameters(parameters));
        }

        @Override
        public VxReq.Instance requestBody(String requestBody) {
            return new VxReq.Instance(this.webClient,
                    this.req.copy().requestBody(requestBody));
        }

        @Override
        public VxReq.Instance extraUrlQueryBuilder(ExtraUrlQuery.ExtraUrlQueryBuilder extraUrlQueryBuilder) {
            return new VxReq.Instance(this.webClient,
                    this.req.copy().extraUrlQueryBuilder(extraUrlQueryBuilder));
        }

        @SuppressWarnings("rawtypes")
        @Override
        public VxReq.Instance statusFallback(HttpStatus httpStatus, Class<? extends FallbackFunction> errorClass) {
            return new VxReq.Instance(this.webClient,
                    this.req.copy().statusFallback(httpStatus, errorClass));
        }

        @SuppressWarnings("rawtypes")
        @Override
        public VxReq.Instance statusSeriesFallback(HttpStatus.Series httpStatusSeries, Class<? extends FallbackFunction> errorClass) {
            return new VxReq.Instance(this.webClient,
                    this.req.copy().statusSeriesFallback(httpStatusSeries, errorClass));
        }

        @SafeVarargs
        public final void get(Handler<AsyncResult<String>>... handlers) {
            request(new VxReq(this.req), VxReq::buildGetRequest, VxReq::buildGetBody, handlers);
        }

        @SafeVarargs
        public final void post(Handler<AsyncResult<String>>... handlers) {
            request(new VxReq(this.req), VxReq::buildPostRequest, VxReq::buildPostBody, handlers);
        }

        public Future<String> get() {
            return request(new VxReq(this.req), VxReq::buildGetRequest, VxReq::buildGetBody);
        }

        public Future<String> post() {
            return request(new VxReq(this.req), VxReq::buildPostRequest, VxReq::buildPostBody);
        }

        @SafeVarargs
        private void request(VxReq vxReq,
                             BiFunction<VxReq, WebClient, HttpRequest<Buffer>> requestBuilder,
                             Function<VxReq, Buffer> bodyBuilder,
                             Handler<AsyncResult<String>>... handlers) {
            requestBuilder.apply(vxReq, this.webClient)
                    .sendBuffer(bodyBuilder.apply(vxReq), vxReq.handle(handlers));
        }

        private Future<String> request(VxReq vxReq,
                                       BiFunction<VxReq, WebClient, HttpRequest<Buffer>> requestBuilder,
                                       Function<VxReq, Buffer> bodyBuilder) {
            Promise<String> promise = Promise.promise();
            requestBuilder.apply(vxReq, this.webClient)
                    .sendBuffer(bodyBuilder.apply(vxReq), vxReq.handle(promise));
            return promise.future();
        }
    }

    private HttpRequest<Buffer> buildGetRequest(WebClient webClient) {
        val parameterMap = fetchParameterMap();
        val requestUrl = concatRequestUrl(parameterMap);
        val headersMap = fetchHeaderMap();
        val query = URL_QUERY_FORMATTER.format(parameterMap, newHashMap());
        return webClient.getAbs(concatUrlQuery(requestUrl, query)).putHeaders(headersMap);
    }

    private Buffer buildGetBody() {
        return null;
    }

    private HttpRequest<Buffer> buildPostRequest(WebClient webClient) {
        val parameterMap = fetchParameterMap();
        val requestUrl = concatRequestUrl(parameterMap);
        val headersMap = fetchHeaderMap();
        return webClient.postAbs(requestUrl).putHeaders(headersMap);
    }

    private Buffer buildPostBody() {
        val content = nullThen(this.requestBody, () ->
                this.contentFormatter.format(fetchParameterMap(), newHashMap()));
        val charset = parseCharset(this.contentFormatter.contentType());
        return Buffer.buffer(content, charset);
    }

    private MultiMap fetchHeaderMap() {
        val headersMap = MultiMap.caseInsensitiveMultiMap();
        val acceptCharsetName = this.acceptCharset.name();
        headersMap.set(ACCEPT_CHARSET, acceptCharsetName);
        val contentType = this.contentFormatter.contentType();
        headersMap.set(CONTENT_TYPE, contentType);
        for (val header : this.headers) {
            checkNull(header.getValue(),
                    () -> headersMap.remove(header.getKey()),
                    xx -> headersMap.set(header.getKey(), header.getValue()));
        }
        return headersMap;
    }

    private String parseCharset(String contentType) {
        return checkNull(MediaType.parse(contentType), UTF_8::name, mediaType ->
                checkNull(mediaType.charset(), UTF_8::name, Charset::name));
    }

    @SafeVarargs
    private Handler<AsyncResult<HttpResponse<Buffer>>> handle(
            Handler<AsyncResult<String>>... handlers) {
        return arResponse -> {
            val promise = Promise.<String>promise();
            if (arResponse.succeeded()) {
                try {
                    val response = arResponse.result();
                    val statusCode = response.statusCode();
                    val responseBody = response.bodyAsString(acceptCharset.name());

                    val statusFallback = statusFallbackMapping
                            .get(HttpStatus.valueOf(statusCode));
                    val statusSeriesFallback = statusSeriesFallbackMapping
                            .get(HttpStatus.Series.valueOf(statusCode));

                    if (nonNull(statusFallback)) {
                        promise.complete(applyFallback(statusFallback,
                                statusCode, responseBody));

                    } else if (nonNull(statusSeriesFallback)) {
                        promise.complete(applyFallback(statusSeriesFallback,
                                statusCode, responseBody));

                    } else promise.complete(responseBody);
                } catch (Exception e) {
                    promise.fail(e);
                }
            } else {
                promise.fail(new VxException(arResponse.cause())); // to remove VxException wrapper
            }

            iterateHandlers(promise, handlers);
        };
    }

    @SuppressWarnings("rawtypes")
    private String applyFallback(Class<? extends FallbackFunction> function,
                                 int statusCode, String responseBody) {
        return toStr(reflectFactory().build(function).apply(
                new Response<>(statusCode, responseBody) {
                    @Override
                    public String responseBodyAsString() {
                        return getResponseBody();
                    }
                }));
    }

    @SafeVarargs
    private void iterateHandlers(Promise<String> promise,
                                 Handler<AsyncResult<String>>... handlers) {
        forArray(handlers).forEachRemaining(handler ->
                notNullThenRun(handler, h -> h.handle(promise.future())));
    }
}
