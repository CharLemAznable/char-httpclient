package com.github.charlemaznable.httpclient.wfclient;

import com.github.charlemaznable.core.lang.Mapp;
import com.github.charlemaznable.httpclient.common.CommonReq;
import com.github.charlemaznable.httpclient.wfclient.elf.GlobalClientElf;
import com.github.charlemaznable.httpclient.wfclient.internal.WfResponseAdapter;
import lombok.experimental.Delegate;
import lombok.val;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
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

public final class WfReq extends CommonReq<WfReq> {

    private static final WebClient globalClient = GlobalClientElf.globalClient();

    @Delegate
    private final WebClient.Builder builder;

    public WfReq() {
        super();
        this.builder = WebClient.builder();
    }

    public WfReq(String baseUrl) {
        super(baseUrl);
        this.builder = WebClient.builder();
    }

    public WebClient buildWebClient() {
        val buildClient = notNullThen(builder, WebClient.Builder::build);
        return nullThen(buildClient, () -> globalClient);
    }

    public WfReq.Instance buildInstance() {
        return new WfReq.Instance(this, buildWebClient());
    }

    public Mono<String> get() {
        return buildInstance().get();
    }

    public Mono<String> post() {
        return buildInstance().post();
    }

    public CompletableFuture<String> getFuture() {
        return buildInstance().getFuture();
    }

    public CompletableFuture<String> postFuture() {
        return buildInstance().postFuture();
    }

    public static final class Instance extends CommonReq.Instance<Instance> {

        private final WebClient webClient;

        public <U extends CommonReq<U>> Instance(CommonReq<U> other, WebClient webClient) {
            super(other);
            this.webClient = webClient;
        }

        @Override
        public WfReq.Instance copy() {
            return new WfReq.Instance(this, webClient);
        }

        public Mono<String> get() {
            return this.execute(this::buildGetRequestSpec);
        }

        public Mono<String> post() {
            return this.execute(this::buildPostRequestSpec);
        }

        public CompletableFuture<String> getFuture() {
            return this.get().toFuture();
        }

        public CompletableFuture<String> postFuture() {
            return this.post().toFuture();
        }

        private Mono<String> execute(Supplier<WebClient.RequestBodyUriSpec> requestSpecSupplier) {
            return requestSpecSupplier.get().exchangeToMono(this::processResponse);
        }

        private WebClient.RequestBodyUriSpec buildGetRequestSpec() {
            val parameterMap = fetchParameterMap();
            val requestUrl = concatRequestUrl(parameterMap);
            val query = URL_QUERY_FORMATTER.format(parameterMap, newHashMap());
            val headersMap = buildHeadersMap();

            val spec = this.webClient.method(HttpMethod.GET);
            spec.uri(concatUrlQuery(requestUrl, query));
            spec.headers(httpHeaders -> httpHeaders.setAll(headersMap));
            return spec;
        }

        private WebClient.RequestBodyUriSpec buildPostRequestSpec() {
            val parameterMap = fetchParameterMap();
            val requestUrl = concatRequestUrl(parameterMap);
            val headersMap = buildHeadersMap();
            val content = nullThen(this.requestBody, () ->
                    this.contentFormatter().format(parameterMap, newHashMap()));
            val contentType = nullThen(headersMap.get(CONTENT_TYPE),
                    DEFAULT_CONTENT_FORMATTER::contentType);

            val spec = this.webClient.method(HttpMethod.POST);
            spec.uri(requestUrl);
            spec.contentType(MediaType.parseMediaType(contentType));
            spec.body(BodyInserters.fromValue(content));
            spec.headers(httpHeaders -> httpHeaders.setAll(headersMap));
            return spec;
        }

        private Map<String, String> buildHeadersMap() {
            val headersMap = Mapp.<String, String>newHashMap();
            val acceptCharsetName = this.acceptCharset().name();
            headersMap.put(ACCEPT_CHARSET, acceptCharsetName);
            val contentType = this.contentFormatter().contentType();
            headersMap.put(CONTENT_TYPE, contentType);
            for (val header : this.headers()) {
                checkNull(header.getValue(),
                        () -> headersMap.remove(header.getKey()),
                        xx -> headersMap.put(header.getKey(), header.getValue()));
            }
            return headersMap;
        }

        private Mono<String> processResponse(ClientResponse clientResponse) {
            return clientResponse.toEntity(byte[].class).map(response ->
                    processResponse(new WfResponseAdapter(response, acceptCharset())));
        }
    }
}
