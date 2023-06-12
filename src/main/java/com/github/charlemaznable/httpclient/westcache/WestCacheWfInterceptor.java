package com.github.charlemaznable.httpclient.westcache;

import com.github.charlemaznable.core.lang.LoadingCachee;
import com.google.common.cache.Cache;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.github.charlemaznable.httpclient.westcache.WestCacheConstant.buildDefaultStatusCodes;
import static com.google.common.cache.CacheBuilder.newBuilder;
import static java.util.Objects.nonNull;

public final class WestCacheWfInterceptor implements ExchangeFilterFunction {

    private final Cache<WestCacheContext, Optional<CacheResponse>> localCache;
    private final Map<WestCacheContext, ClientResponse> clientResponseMap = Maps.newConcurrentMap();
    @Getter
    private final Set<Integer> cachedStatusCodes = buildDefaultStatusCodes();

    public WestCacheWfInterceptor() {
        this(2 ^ 8, 60);
    }

    public WestCacheWfInterceptor(long localMaximumSize, long localExpireSeconds) {
        this.localCache = newBuilder()
                .maximumSize(localMaximumSize)
                .expireAfterWrite(localExpireSeconds, TimeUnit.SECONDS)
                .build();
    }

    @Nonnull
    @Override
    public Mono<ClientResponse> filter(@Nonnull ClientRequest request, @Nonnull ExchangeFunction next) {
        val contextOptional = request.attribute(WestCacheContext.class.getName());
        if (contextOptional.isEmpty()) return next.exchange(request);

        val context = (WestCacheContext) contextOptional.get();
        return Mono.justOrEmpty(LoadingCachee.get(localCache, context, () -> {
                    val cachedItem = context.cacheGet();
                    if (nonNull(cachedItem) && cachedItem.getObject().isPresent()) {
                        // westcache命中, 且缓存非空
                        return Optional.of((CacheResponse) cachedItem.getObject().get());
                    }

                    return next.exchange(request).flatMap(response -> {
                        val code = response.statusCode().value();
                        if (!cachedStatusCodes.contains(code)) {
                            clientResponseMap.put(context, response);
                            return Mono.empty();
                        }

                        return response.toEntity(String.class).map(entity -> {
                            val cacheResponse = new CacheResponse();
                            cacheResponse.setCode(entity.getStatusCode().value());
                            cacheResponse.setHeaders(entity.getHeaders());
                            cacheResponse.setBody(entity.getBody());
                            context.cachePut(cacheResponse);
                            return cacheResponse;
                        });
                    }).blockOptional();
                }))
                .map(cacheResponse -> ClientResponse
                        .create(HttpStatusCode.valueOf(cacheResponse.getCode()))
                        .headers(httpHeaders -> cacheResponse.getHeaders().forEach(httpHeaders::addAll))
                        .body(cacheResponse.getBody()).build())
                .switchIfEmpty(Mono.defer(() -> Mono
                        .justOrEmpty(clientResponseMap.remove(context))
                        .switchIfEmpty(Mono.defer(() -> next.exchange(request)))));
    }

    @Getter
    @Setter
    public static final class CacheResponse {
        private int code;
        private Map<String, List<String>> headers;
        private String body;
    }
}
