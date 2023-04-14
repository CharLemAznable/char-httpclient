package com.github.charlemaznable.httpclient.vxclient.elf;

import com.github.charlemaznable.httpclient.logging.LoggingVxInterceptor;
import com.github.charlemaznable.httpclient.westcache.WestCacheVxInterceptor;
import com.google.common.cache.LoadingCache;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.impl.WebClientInternal;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.ServiceLoader;

import static com.github.charlemaznable.core.lang.LoadingCachee.get;
import static com.github.charlemaznable.core.lang.LoadingCachee.simpleCache;
import static com.github.charlemaznable.httpclient.westcache.WestCacheConstant.HAS_WESTCACHE;
import static com.google.common.cache.CacheLoader.from;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class VertxScopeClientElf {

    private static final VertxScopeClientBuilder instance;
    private static final LoadingCache<Vertx, WebClient>
            scopeClientCache = simpleCache(from(VertxScopeClientElf::buildScopeClient));

    static {
        instance = findBuilder();
    }

    public static WebClient scopeClient(Vertx vertx) {
        return get(scopeClientCache, vertx);
    }

    private static WebClient buildScopeClient(Vertx vertx) {
        return instance.build(vertx);
    }

    private static VertxScopeClientBuilder findBuilder() {
        val builders = ServiceLoader.load(VertxScopeClientBuilder.class).iterator();
        if (!builders.hasNext()) return new DefaultVertxScopeClientBuilder();

        val result = builders.next();
        if (builders.hasNext())
            throw new IllegalStateException("Multiple VertxScopeClientBuilder Found");
        return result;
    }

    private static final class DefaultVertxScopeClientBuilder implements VertxScopeClientBuilder {

        @Override
        public WebClient build(Vertx vertx) {
            val client = (WebClientInternal) WebClient.create(vertx,
                    new WebClientOptions().setShared(true));
            client.addInterceptor(new LoggingVxInterceptor());
            if (HAS_WESTCACHE) client.addInterceptor(new WestCacheVxInterceptor(vertx));
            return client;
        }
    }
}
