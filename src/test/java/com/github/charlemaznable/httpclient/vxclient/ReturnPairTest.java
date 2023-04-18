package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonReturnPairTest;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class ReturnPairTest extends CommonReturnPairTest {

    @SneakyThrows
    @Test
    public void testPair(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));

        val httpClient = vxLoader.getClient(PairHttpClient.class);

        CompositeFuture.all(newArrayList(
                httpClient.sampleFutureStatusAndBean().onSuccess(pair -> test.verify(() -> {
                    assertEquals(HttpStatus.OK.value(), pair.getKey());
                    assertEquals("John", pair.getValue().getName());
                })),
                httpClient.sampleFutureRawAndBean().onSuccess(rawPair -> test.verify(() -> {
                    assertEquals(json(new Bean("Doe")), rawPair.getKey());
                    assertEquals("Doe", rawPair.getValue().getName());
                }))
        )).onComplete(result -> {
            shutdownMockWebServer();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    @VxClient
    @Mapping("${root}:41194")
    public interface PairHttpClient {

        Future<Pair<Integer, Bean>> sampleFutureStatusAndBean();

        Future<Pair<String, Bean>> sampleFutureRawAndBean();
    }
}
