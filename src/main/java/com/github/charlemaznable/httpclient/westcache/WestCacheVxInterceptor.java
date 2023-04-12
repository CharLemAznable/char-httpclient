package com.github.charlemaznable.httpclient.westcache;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.impl.headers.HeadersMultiMap;
import io.vertx.ext.web.client.impl.HttpContext;
import io.vertx.ext.web.client.impl.HttpResponseImpl;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static java.util.Objects.isNull;

@AllArgsConstructor
public final class WestCacheVxInterceptor implements Handler<HttpContext<?>> {

    private static final String IS_CACHE_DISPATCH = "cache.dispatch";

    private final Vertx vertx;

    @SuppressWarnings("unchecked")
    @Override
    public void handle(HttpContext<?> context) {
        switch (context.phase()) {
            case CREATE_REQUEST -> handleCreateRequest((HttpContext<Buffer>) context);
            case DISPATCH_RESPONSE -> handleDispatchResponse((HttpContext<Buffer>) context);
            default -> context.next();
        }
    }

    private void handleCreateRequest(HttpContext<Buffer> context) {
        val option = context.<WestCacheOption>get(WestCacheOption.class.getName());
        val cacheKey = context.<WestCacheKey>get(WestCacheKey.class.getName());
        if (isNull(option) || isNull(cacheKey)) {
            context.next();
            return;
        }
        val promise = Promise.<WestCacheItem>promise();
        vertx.executeBlocking(block -> block.complete(
                option.getManager().get(option, cacheKey.getKey())), promise);
        promise.future()
                .map(item -> Optional.ofNullable((CacheResponse) item.orNull()))
                .onComplete(result -> {
                    if (result.succeeded() && result.result().isPresent()) {
                        context.set(IS_CACHE_DISPATCH, true);
                        val cacheResponse = result.result().get();
                        context.dispatchResponse(new HttpResponseImpl<>(
                                HttpVersion.valueOf(cacheResponse.getVersion()),
                                cacheResponse.getStatusCode(),
                                cacheResponse.getStatusMessage(),
                                cacheResponse.buildResponseHeaders(),
                                MultiMap.caseInsensitiveMultiMap(),
                                Collections.emptyList(),
                                cacheResponse.getBody().buildBuffer(),
                                Collections.emptyList()
                        ));
                    } else {
                        context.next();
                    }
                });
    }

    private void handleDispatchResponse(HttpContext<Buffer> context) {
        if (context.get(IS_CACHE_DISPATCH) == Boolean.TRUE) {
            context.next();
            return;
        }
        val option = context.<WestCacheOption>get(WestCacheOption.class.getName());
        val cacheKey = context.<WestCacheKey>get(WestCacheKey.class.getName());
        if (isNull(option) || isNull(cacheKey)) {
            context.next();
            return;
        }
        val response = context.response();
        vertx.executeBlocking(block -> {
            val cacheResponse = new CacheResponse();
            cacheResponse.setVersion(response.version().name());
            cacheResponse.setStatusCode(response.statusCode());
            cacheResponse.setStatusMessage(response.statusMessage());
            cacheResponse.readFromResponseHeaders(response.headers());
            val responseBody = nullThen(response.body(), Buffer::buffer);
            val cacheResponseBody = new CacheResponseBody();
            cacheResponseBody.readFromBuffer(responseBody);
            cacheResponse.setBody(cacheResponseBody);
            @SuppressWarnings("Guava") val optional = com.google.common.base.Optional.of(cacheResponse);
            option.getManager().put(option, cacheKey.getKey(), new WestCacheItem(optional, option));
            block.complete();
        }, result -> context.next());
    }

    @Getter
    @Setter
    public static final class CacheResponse {
        private String version;
        private int statusCode;
        private String statusMessage;
        private Map<String, List<String>> responseHeaders;
        private CacheResponseBody body;

        public void readFromResponseHeaders(MultiMap responseHeaders) {
            val headers = new HashMap<String, List<String>>();
            responseHeaders.forEach((key, value) -> {
                List<String> values = headers.get(key);
                if (isNull(values)) {
                    values = new ArrayList<>();
                    headers.put(key, values);
                }
                values.add(value);
            });
            setResponseHeaders(headers);
        }

        public MultiMap buildResponseHeaders() {
            val responseHeaders = HeadersMultiMap.httpHeaders();
            for (val entry : getResponseHeaders().entrySet()) {
                for (val value : entry.getValue()) {
                    responseHeaders.add(entry.getKey(), value);
                }
            }
            return responseHeaders;
        }
    }

    @Getter
    @Setter
    public static final class CacheResponseBody {
        private byte[] content;

        @SneakyThrows
        public void readFromBuffer(Buffer responseBody) {
            setContent(responseBody.getBytes());
        }

        public Buffer buildBuffer() {
            return Buffer.buffer(getContent());
        }
    }
}
