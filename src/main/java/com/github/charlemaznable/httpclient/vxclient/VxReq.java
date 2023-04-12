package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.httpclient.common.CommonReq;
import com.github.charlemaznable.httpclient.common.FallbackFunction;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import lombok.experimental.Delegate;
import lombok.val;

import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThenRun;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static com.github.charlemaznable.core.net.Url.concatUrlQuery;
import static com.github.charlemaznable.httpclient.common.CommonConstant.ACCEPT_CHARSET;
import static com.github.charlemaznable.httpclient.common.CommonConstant.CONTENT_TYPE;
import static com.github.charlemaznable.httpclient.common.CommonConstant.URL_QUERY_FORMATTER;
import static com.google.common.collect.Iterators.forArray;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

public class VxReq extends CommonReq<VxReq> {

    private final Vertx vertx;
    @Delegate
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

    public WebClient buildWebClient() {
        return WebClient.create(requireNonNull(vertx), webClientOptions);
    }

    public VxReq.Instance buildInstance() {
        return new Instance(this, buildWebClient());
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

    public static final class Instance extends CommonReq.Instance<Instance> {

        private final WebClient webClient;

        public <U extends CommonReq<U>> Instance(CommonReq<U> other, WebClient webClient) {
            super(other);
            this.webClient = webClient;
        }

        @Override
        public VxReq.Instance copy() {
            return new VxReq.Instance(this, webClient);
        }

        @SafeVarargs
        public final void get(Handler<AsyncResult<String>>... handlers) {
            request(this::buildGetRequest, this::buildGetBody, handlers);
        }

        @SafeVarargs
        public final void post(Handler<AsyncResult<String>>... handlers) {
            request(this::buildPostRequest, this::buildPostBody, handlers);
        }

        public Future<String> get() {
            return request(this::buildGetRequest, this::buildGetBody);
        }

        public Future<String> post() {
            return request(this::buildPostRequest, this::buildPostBody);
        }

        @SafeVarargs
        private void request(Function<WebClient, HttpRequest<Buffer>> requestBuilder,
                             Supplier<Buffer> bodySupplier,
                             Handler<AsyncResult<String>>... handlers) {
            requestBuilder.apply(this.webClient)
                    .sendBuffer(bodySupplier.get(), handle(handlers));
        }

        private Future<String> request(Function<WebClient, HttpRequest<Buffer>> requestBuilder,
                                       Supplier<Buffer> bodySupplier) {
            Promise<String> promise = Promise.promise();
            requestBuilder.apply(this.webClient)
                    .sendBuffer(bodySupplier.get(), handle(promise));
            return promise.future();
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
                    this.contentFormatter().format(fetchParameterMap(), newHashMap()));
            val charset = parseCharset(this.contentFormatter().contentType());
            return Buffer.buffer(content, charset);
        }

        private MultiMap fetchHeaderMap() {
            val headersMap = MultiMap.caseInsensitiveMultiMap();
            val acceptCharsetName = this.acceptCharset().name();
            headersMap.set(ACCEPT_CHARSET, acceptCharsetName);
            val contentType = this.contentFormatter().contentType();
            headersMap.set(CONTENT_TYPE, contentType);
            for (val header : this.headers()) {
                checkNull(header.getValue(),
                        () -> headersMap.remove(header.getKey()),
                        xx -> headersMap.set(header.getKey(), header.getValue()));
            }
            return headersMap;
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
                        val responseBody = response.bodyAsString(this.acceptCharset().name());

                        val statusFallback = this.statusFallbackMapping()
                                .get(HttpStatus.valueOf(statusCode));
                        val statusSeriesFallback = this.statusSeriesFallbackMapping()
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
                    promise.fail(arResponse.cause());
                }

                iterateHandlers(promise, handlers);
            };
        }

        private String applyFallback(FallbackFunction<?> function,
                                     int statusCode, String responseBody) {
            return toStr(function.apply(
                    new FallbackFunction.Response<>(statusCode, responseBody) {
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
}
