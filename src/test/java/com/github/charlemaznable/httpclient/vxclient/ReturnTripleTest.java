package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonReturnTripleTest;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class ReturnTripleTest extends CommonReturnTripleTest {

    @Test
    public void testTriple(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));

        val httpClient = vxLoader.getClient(TripleHttpClient.class);

        CompositeFuture.all(newArrayList(
                httpClient.sampleFutureStatusCodeAndBean().onSuccess(triple -> test.verify(() -> {
                    assertEquals(HttpStatus.OK.value(), triple.getLeft());
                    assertEquals(HttpStatus.OK, triple.getMiddle());
                    assertEquals("John", triple.getRight().getName());
                })),
                httpClient.sampleFutureRawStreamAndBean().onSuccess(rawTriple -> test.verify(() -> {
                    assertEquals(json(new Bean("Doe")), rawTriple.getLeft().toString());
                    assertEquals(json(new Bean("Doe")), rawTriple.getMiddle());
                    assertEquals("Doe", rawTriple.getRight().getName());
                }))
        )).onComplete(result -> {
            shutdownMockWebServer();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    @VxClient
    @Mapping("${root}:41195")
    public interface TripleHttpClient {

        Future<Triple<Integer, HttpStatus, Bean>> sampleFutureStatusCodeAndBean();

        Future<Triple<Buffer, String, Bean>> sampleFutureRawStreamAndBean();
    }
}
