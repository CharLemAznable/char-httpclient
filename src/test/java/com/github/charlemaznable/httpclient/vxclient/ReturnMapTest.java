package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonReturnMapTest;
import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.Map;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(VertxExtension.class)
public class ReturnMapTest extends CommonReturnMapTest {

    @SuppressWarnings("rawtypes")
    @Test
    public void testMap(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));

        val httpClient = vxLoader.getClient(MapHttpClient.class);

        Future.all(newArrayList(
                httpClient.sampleFutureMap().onSuccess(map -> test.verify(() -> {
                    Map beanMap = (Map) map.get("John");
                    assertEquals("Doe", beanMap.get("name"));
                })),
                httpClient.sampleFutureMapNull().onSuccess(map -> test.verify(() -> assertNull(map)))
        )).onComplete(result -> {
            shutdownMockWebServer();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    @VxClient
    @Mapping("${root}:41193")
    public interface MapHttpClient {

        Future<HashMap<String, Object>> sampleFutureMap();

        Future<Map<String, Object>> sampleFutureMapNull();
    }
}
