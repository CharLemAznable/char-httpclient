package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.httpclient.common.CommonReq;
import com.github.charlemaznable.httpclient.vxclient.internal.VxResponseAdapter;
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

import java.util.function.Supplier;

import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThenRun;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.net.Url.concatUrlQuery;
import static com.github.charlemaznable.httpclient.common.CommonConstant.ACCEPT_CHARSET;
import static com.github.charlemaznable.httpclient.common.CommonConstant.CONTENT_TYPE;
import static com.github.charlemaznable.httpclient.common.CommonConstant.URL_QUERY_FORMATTER;
import static com.google.common.collect.Iterators.forArray;
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
        private void request(Supplier<HttpRequest<Buffer>> requestBuilder,
                             Supplier<Buffer> bodySupplier,
                             Handler<AsyncResult<String>>... handlers) {
            requestBuilder.get().sendBuffer(bodySupplier.get(), handle(handlers));
        }

        private Future<String> request(Supplier<HttpRequest<Buffer>> requestBuilder,
                                       Supplier<Buffer> bodySupplier) {
            Promise<String> promise = Promise.promise();
            requestBuilder.get().sendBuffer(bodySupplier.get(), handle(promise));
            return promise.future();
        }

        private HttpRequest<Buffer> buildGetRequest() {
            val parameterMap = fetchParameterMap();
            val requestUrl = concatRequestUrl(parameterMap);
            val headersMap = fetchHeaderMap();
            val query = URL_QUERY_FORMATTER.format(parameterMap, newHashMap());
            return this.webClient.getAbs(concatUrlQuery(requestUrl, query)).putHeaders(headersMap);
        }

        private Buffer buildGetBody() {
            return null;
        }

        private HttpRequest<Buffer> buildPostRequest() {
            val parameterMap = fetchParameterMap();
            val requestUrl = concatRequestUrl(parameterMap);
            val headersMap = fetchHeaderMap();
            return this.webClient.postAbs(requestUrl).putHeaders(headersMap);
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
                        promise.complete(processResponse(new VxResponseAdapter(
                                arResponse.result(), acceptCharset())));
                    } catch (Exception e) {
                        promise.fail(e);
                    }
                } else {
                    promise.fail(arResponse.cause());
                }

                iterateHandlers(promise, handlers);
            };
        }

        @SafeVarargs
        private void iterateHandlers(Promise<String> promise,
                                     Handler<AsyncResult<String>>... handlers) {
            forArray(handlers).forEachRemaining(handler ->
                    notNullThenRun(handler, h -> h.handle(promise.future())));
        }
    }
}
