package com.github.charlemaznable.httpclient.westcache;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.github.charlemaznable.httpclient.ohclient.internal.OhResponseBody;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.RealResponseBody;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static okio.Okio.buffer;
import static okio.Okio.source;

public final class WestCacheOhInterceptor implements Interceptor {

    @NotNull
    @Override
    public Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        val request = chain.request();
        val option = request.tag(WestCacheOption.class);
        val cacheKey = request.tag(WestCacheKey.class);
        if (isNull(option) || isNull(cacheKey)) return chain.proceed(request);

        val item = option.getManager().get(option, cacheKey.getKey(), () -> {
            val response = chain.proceed(request);
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
            @SuppressWarnings("Guava") val optional = Optional.of(cacheResponse);
            return new WestCacheItem(optional, option);
        });
        val cacheResponse = (CacheResponse) item.orNull();
        if (isNull(cacheResponse)) return chain.proceed(request);

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
}
