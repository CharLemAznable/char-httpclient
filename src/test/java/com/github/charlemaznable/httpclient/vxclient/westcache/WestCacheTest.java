package com.github.charlemaznable.httpclient.vxclient.westcache;

import com.github.charlemaznable.core.lang.Await;
import com.github.charlemaznable.httpclient.common.westcache.CommonWestCacheTest;
import io.vertx.core.CompositeFuture;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ExtendWith(VertxExtension.class)
@SpringJUnitConfig(WestCacheConfiguration.class)
public class WestCacheTest extends CommonWestCacheTest {

    @Autowired
    private NoWestCacheClient noWestCacheClient;
    @Autowired
    private WestCacheClient westCacheClient;

    @Test
    public void testWestCache(VertxTestContext test) {
        startMockWebServer();

        val noWestCache = noWestCacheClient.sample().compose(result1 -> {
            Await.awaitForMillis(100);
            return noWestCacheClient.sample().onSuccess(result2 ->
                    test.verify(() -> assertNotEquals(result1, result2)));
        });

        val noneCache = westCacheClient.sampleNone().compose(result1 -> {
            Await.awaitForMillis(100);
            return westCacheClient.sampleNone().onSuccess(result2 ->
                    test.verify(() -> assertNotEquals(result1, result2)));
        });

        val cache = westCacheClient.sample().compose(ignored -> {
            val cacheSample1 = westCacheClient.sample();
            Await.awaitForMillis(500);
            val cacheSample2 = westCacheClient.sample();
            assertNotSame(cacheSample1, cacheSample2);

            return cacheSample1.compose(result1 -> {
                Await.awaitForMillis(100);
                return cacheSample2.onSuccess(result2 ->
                        test.verify(() -> assertEquals(result1, result2)));
            });
        });

        CompositeFuture.all(newArrayList(
                noWestCache, noneCache, cache
        )).onComplete(result -> {
            shutdownMockWebServer();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }
}
