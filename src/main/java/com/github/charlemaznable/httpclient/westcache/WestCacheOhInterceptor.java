package com.github.charlemaznable.httpclient.westcache;

import com.github.charlemaznable.core.lang.LoadingCachee;
import com.github.charlemaznable.httpclient.ohclient.internal.OhResponseBody;
import com.google.common.cache.Cache;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.RealResponseBody;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static com.github.charlemaznable.httpclient.westcache.WestCacheConstant.DEFAULT_CACHED_STATUS_CODES;
import static com.google.common.cache.CacheBuilder.newBuilder;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static okio.Okio.buffer;
import static okio.Okio.source;

public final class WestCacheOhInterceptor implements Interceptor {

    private final Cache<WestCacheContext, Optional<CacheResponse>> localCache;

    public WestCacheOhInterceptor() {
        this(2 ^ 8, 60);
    }

    public WestCacheOhInterceptor(long localMaximumSize, long localExpireSeconds) {
        this.localCache = newBuilder()
                .maximumSize(localMaximumSize)
                .expireAfterWrite(localExpireSeconds, TimeUnit.SECONDS)
                .build();
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        val request = chain.request();
        val context = request.tag(WestCacheContext.class);
        if (isNull(context)) return chain.proceed(request);

        val networkResponse = new NetworkResponse();
        val cachedOptional = LoadingCachee.get(localCache, context, () -> {
            val cachedItem = context.cacheGet();
            if (nonNull(cachedItem) && cachedItem.getObject().isPresent()) {
                // westcache命中, 且缓存非空
                return Optional.of((CacheResponse) cachedItem.getObject().get());
            }

            val response = chain.proceed(request);
            val code = response.code();
            if (!DEFAULT_CACHED_STATUS_CODES.contains(code)) {
                networkResponse.setResponse(response);
                return Optional.empty();
            }

            val cacheResponse = new CacheResponse();
            cacheResponse.setProtocol(response.protocol().toString());
            cacheResponse.setCode(response.code());
            cacheResponse.setMessage(response.message());
            cacheResponse.setHeaders(response.headers().toMultimap());
            val responseBody = notNullThen(response.body(), OhResponseBody::new);
            if (nonNull(response.body())) response.close();
            val cacheResponseBody = new CacheResponseBody();
            cacheResponseBody.setContentType(toStr(responseBody.contentType()));
            cacheResponseBody.setContentLength(responseBody.contentLength());
            cacheResponseBody.readFromResponseBody(responseBody);
            cacheResponse.setBody(cacheResponseBody);
            context.cachePut(cacheResponse);
            return Optional.of(cacheResponse);
        });
        if (cachedOptional.isEmpty()) return nullThen(
                networkResponse.getResponse(), proceed(chain, request));

        val cacheResponse = cachedOptional.get();
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.get(cacheResponse.getProtocol()))
                .code(cacheResponse.getCode())
                .message(cacheResponse.getMessage())
                .headers(cacheResponse.buildHeaders())
                .body(cacheResponse.getBody().buildResponseBody())
                .build();
    }

    @Getter
    @Setter
    public static final class CacheResponse {
        private String protocol;
        private int code;
        private String message;
        private Map<String, List<String>> headers;
        private CacheResponseBody body;

        public Headers buildHeaders() {
            val headersBuilder = new Headers.Builder();
            for (val entry : getHeaders().entrySet()) {
                for (val value : entry.getValue()) {
                    headersBuilder.add(entry.getKey(), value);
                }
            }
            return headersBuilder.build();
        }
    }

    @Getter
    @Setter
    public static final class CacheResponseBody {
        private String contentType;
        private long contentLength;
        private byte[] content;

        @SneakyThrows
        public void readFromResponseBody(ResponseBody responseBody) {
            setContent(responseBody.bytes());
        }

        public ResponseBody buildResponseBody() {
            return new OhResponseBody(new RealResponseBody(
                    getContentType(), getContentLength(),
                    buffer(source(new ByteArrayInputStream(getContent())))));
        }
    }

    @Getter
    @Setter
    private static final class NetworkResponse {
        private Response response;
    }

    private static Supplier<Response> proceed(Interceptor.Chain chain, Request request) {
        return new ProceedSupplier(chain, request);
    }

    @AllArgsConstructor
    private static final class ProceedSupplier implements Supplier<Response> {

        private final Interceptor.Chain chain;
        private final Request request;

        @SneakyThrows
        @Override
        public Response get() {
            return chain.proceed(request);
        }
    }
}
