package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonReturnErrorTest;
import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class ReturnErrorTest extends CommonReturnErrorTest {

    @Test
    public void testError(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));

        val httpClient = vxLoader.getClient(ErrorHttpClient.class);

        assertThrows(IllegalStateException.class, httpClient::sampleObject);
        assertThrows(IllegalStateException.class, httpClient::sampleFuture);
        assertThrows(IllegalStateException.class, httpClient::sampleFutureT);
        assertThrows(IllegalStateException.class, httpClient::sampleFutureList);
        assertThrows(IllegalStateException.class, httpClient::sampleFutureListT);
        assertThrows(IllegalStateException.class, httpClient::sampleFuturePair);
        assertThrows(IllegalStateException.class, httpClient::sampleFuturePairTU);
        assertThrows(IllegalStateException.class, httpClient::sampleFutureTriple);
        assertThrows(IllegalStateException.class, httpClient::sampleFutureTripleTUV);

        CompositeFuture.all(newArrayList(
                Future.future(f -> httpClient.sampleMapError().onFailure(ex -> {
                    test.verify(() -> assertTrue(ex instanceof IllegalArgumentException));
                    f.complete();
                })),
                httpClient.sampleMap().onSuccess(response -> test.verify(() -> assertEquals("Doe", response.get("John"))))
        )).onComplete(result -> {
            shutdownMockWebServer();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    @SuppressWarnings({"UnusedReturnValue", "rawtypes"})
    @VxClient
    @Mapping("${root}:41196")
    public interface ErrorHttpClient {

        Object sampleObject();

        Future sampleFuture();

        <T> Future<T> sampleFutureT();

        Future<List> sampleFutureList();

        <T> Future<List<T>> sampleFutureListT();

        Future<Pair> sampleFuturePair();

        <T, U> Future<Pair<T, U>> sampleFuturePairTU();

        Future<Triple> sampleFutureTriple();

        <T, U, V> Future<Triple<T, U, V>> sampleFutureTripleTUV();

        Future<Map> sampleMapError();

        Future<Map> sampleMap();
    }
}
