package com.github.charlemaznable.httpclient.westcache;

import com.github.charlemaznable.core.lang.LoadingCachee;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.impl.headers.HeadersMultiMap;
import io.vertx.ext.web.client.impl.HttpContext;
import io.vertx.ext.web.client.impl.HttpResponseImpl;
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
import java.util.concurrent.TimeUnit;

import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.google.common.cache.CacheBuilder.newBuilder;
import static com.google.common.cache.CacheLoader.from;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public final class WestCacheVxInterceptor implements Handler<HttpContext<?>> {

    private static final String IS_CACHE_DISPATCH = "cache.dispatch";

    private final Vertx vertx;
    private final LoadingCache<WestCacheContext, Optional<CacheResponse>> localCache;
    private final Map<WestCacheContext, WestCacheContext> lockMap = Maps.newConcurrentMap();

    public WestCacheVxInterceptor(Vertx vertx) {
        this(vertx, 2 ^ 8, 60);
    }

    public WestCacheVxInterceptor(Vertx vertx, long localMaximumSize, long localExpireSeconds) {
        this.vertx = vertx;
        this.localCache = newBuilder()
                .maximumSize(localMaximumSize)
                .expireAfterWrite(localExpireSeconds, TimeUnit.SECONDS)
                .build(from(context -> {
                    val cachedItem = context.cacheGet();
                    if (nonNull(cachedItem) && cachedItem.getObject().isPresent()) {
                        // westcache命中, 且缓存非空
                        return Optional.of((CacheResponse) cachedItem.getObject().get());
                    }
                    return Optional.empty();
                }));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handle(HttpContext<?> httpContext) {
        val context = httpContext.<WestCacheContext>get(WestCacheContext.class.getName());
        if (isNull(context)) {
            httpContext.next();
            return;
        }
        switch (httpContext.phase()) {
            case CREATE_REQUEST -> handleCreateRequest((HttpContext<Buffer>) httpContext, context);
            case DISPATCH_RESPONSE -> handleDispatchResponse((HttpContext<Buffer>) httpContext, context);
            default -> httpContext.next();
        }
    }

    private void handleCreateRequest(HttpContext<Buffer> httpContext, WestCacheContext context) {
        vertx.<Optional<CacheResponse>>executeBlocking(block -> {
            val cachedOptional = LoadingCachee.get(localCache, context);
            if (cachedOptional.isEmpty()) lockMap.putIfAbsent(context, context);
            block.complete(cachedOptional);
        }, result -> {
            if (result.succeeded()) {
                if (result.result().isPresent()) {
                    httpContext.set(IS_CACHE_DISPATCH, true);
                    val cacheResponse = result.result().get();
                    httpContext.dispatchResponse(new HttpResponseImpl<>(
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
                    if (lockMap.get(context) == context) {
                        httpContext.next();
                    } else {
                        // re-create request for waiting
                        httpContext.createRequest(httpContext.requestOptions());
                    }
                }
            } else {
                httpContext.next();
            }
        });
    }

    private void handleDispatchResponse(HttpContext<Buffer> httpContext, WestCacheContext context) {
        if (httpContext.get(IS_CACHE_DISPATCH) == Boolean.TRUE) {
            httpContext.next();
            return;
        }
        val response = httpContext.response();
        vertx.<Void>executeBlocking(block -> {
            val cacheResponse = new CacheResponse();
            cacheResponse.setVersion(response.version().name());
            cacheResponse.setStatusCode(response.statusCode());
            cacheResponse.setStatusMessage(response.statusMessage());
            cacheResponse.readFromResponseHeaders(response.headers());
            val responseBody = nullThen(response.body(), Buffer::buffer);
            val cacheResponseBody = new CacheResponseBody();
            cacheResponseBody.readFromBuffer(responseBody);
            cacheResponse.setBody(cacheResponseBody);
            context.cachePut(cacheResponse);
            localCache.put(context, Optional.of(cacheResponse));
            lockMap.remove(context);
            block.complete();
        }, result -> httpContext.next());
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
            val headersMap = new HashMap<String, List<String>>();
            responseHeaders.forEach((key, value) -> {
                List<String> values = headersMap.get(key);
                if (isNull(values)) {
                    values = new ArrayList<>();
                    headersMap.put(key, values);
                }
                values.add(value);
            });
            setResponseHeaders(headersMap);
        }

        public MultiMap buildResponseHeaders() {
            val headersMultiMap = HeadersMultiMap.httpHeaders();
            for (val entry : getResponseHeaders().entrySet()) {
                for (val value : entry.getValue()) {
                    headersMultiMap.add(entry.getKey(), value);
                }
            }
            return headersMultiMap;
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