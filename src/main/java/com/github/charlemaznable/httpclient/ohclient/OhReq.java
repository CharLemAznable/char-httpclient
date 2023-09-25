package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.common.CommonReq;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import com.github.charlemaznable.httpclient.ohclient.elf.GlobalClientElf;
import com.github.charlemaznable.httpclient.ohclient.internal.OhCallbackFuture;
import com.github.charlemaznable.httpclient.ohclient.internal.OhResponseAdapter;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.val;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.net.Url.concatUrlQuery;
import static com.github.charlemaznable.httpclient.common.CommonConstant.ACCEPT_CHARSET;
import static com.github.charlemaznable.httpclient.common.CommonConstant.CONTENT_TYPE;
import static com.github.charlemaznable.httpclient.common.CommonConstant.DEFAULT_CONTENT_FORMATTER;
import static com.github.charlemaznable.httpclient.common.CommonConstant.URL_QUERY_FORMATTER;

public final class OhReq extends CommonReq<OhReq> {

    private static final OkHttpClient globalClient = GlobalClientElf.globalClient();

    @Delegate
    private final OkHttpClient.Builder builder;

    public OhReq() {
        super();
        this.builder = new OkHttpClient.Builder();
    }

    public OhReq(String baseUrl) {
        super(baseUrl);
        this.builder = new OkHttpClient.Builder();
    }

    public OkHttpClient buildHttpClient() {
        val buildClient = notNullThen(builder, OkHttpClient.Builder::build);
        return nullThen(buildClient, () -> globalClient);
    }

    public Instance buildInstance() {
        return new Instance(this, buildHttpClient());
    }

    public String get() {
        return buildInstance().get();
    }

    public String post() {
        return buildInstance().post();
    }

    public CompletableFuture<String> getFuture() {
        return buildInstance().getFuture();
    }

    public CompletableFuture<String> postFuture() {
        return buildInstance().postFuture();
    }

    public static final class Instance extends CommonReq.Instance<Instance> {

        private final OkHttpClient httpClient;

        public <U extends CommonReq<U>> Instance(CommonReq<U> other, OkHttpClient httpClient) {
            super(other);
            this.httpClient = httpClient;
        }

        @Override
        public OhReq.Instance copy() {
            return new OhReq.Instance(this, httpClient);
        }

        public String get() {
            return this.execute(this::buildGetRequest);
        }

        public String post() {
            return this.execute(this::buildPostRequest);
        }

        public CompletableFuture<String> getFuture() {
            return this.enqueue(this::buildGetRequest);
        }

        public CompletableFuture<String> postFuture() {
            return this.enqueue(this::buildPostRequest);
        }

        @SneakyThrows
        private String execute(Supplier<Request> requestSupplier) {
            return this.processResponse(httpClient.newCall(requestSupplier.get()).execute());
        }

        private CompletableFuture<String> enqueue(Supplier<Request> requestSupplier) {
            val future = new OhCallbackFuture<>(this::processResponse);
            httpClient.newCall(requestSupplier.get()).enqueue(future);
            return future;
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
                    this.contentFormatter().format(parameterMap, newHashMap()));
            val contentType = nullThen(headersBuilder.get(CONTENT_TYPE),
                    DEFAULT_CONTENT_FORMATTER::contentType);
            requestBuilder.method(HttpMethod.POST.toString(),
                    RequestBody.create(content, MediaType.parse(contentType)));
            requestBuilder.url(requestUrl);
            return requestBuilder.build();
        }

        private Headers.Builder buildHeadersBuilder() {
            val headersBuilder = new Headers.Builder();
            val acceptCharsetName = this.acceptCharset().name();
            headersBuilder.set(ACCEPT_CHARSET, acceptCharsetName);
            val contentType = this.contentFormatter().contentType();
            headersBuilder.set(CONTENT_TYPE, contentType);
            for (val header : this.headers()) {
                checkNull(header.getValue(),
                        () -> headersBuilder.removeAll(header.getKey()),
                        xx -> headersBuilder.set(header.getKey(), header.getValue()));
            }
            return headersBuilder;
        }

        private String processResponse(Response response) {
            return processResponse(new OhResponseAdapter(response, acceptCharset()));
        }
    }
}
