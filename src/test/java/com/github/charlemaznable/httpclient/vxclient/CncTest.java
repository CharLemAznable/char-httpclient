package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.core.config.Arguments;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CncRequest;
import com.github.charlemaznable.httpclient.common.CncResponse;
import com.github.charlemaznable.httpclient.common.CommonCncTest;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(VertxExtension.class)
public class CncTest extends CommonCncTest {

    @BeforeAll
    public static void beforeAll() {
        Arguments.initial("--port=41200");
    }

    @AfterAll
    public static void afterAll() {
        Arguments.initial();
    }

    @SneakyThrows
    @Test
    public void testCncClient(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));

        val client = vxLoader.getClient(CncClient.class);
        val errorClient = vxLoader.getClient(CncErrorClient.class);

        Future.all(newArrayList(
                client.sample1(new TestRequest()).onSuccess(response -> test.verify(() -> assertEquals(CONTENT, response.getContent()))),
                client.sample2(new TestRequest()).onSuccess(pair -> test.verify(() -> {
                    assertEquals(HttpStatus.OK, pair.getLeft());
                    assertEquals(CONTENT, pair.getRight().getContent());
                })),
                Future.future(f -> test.verify(() -> {
                    assertThrows(IllegalStateException.class, errorClient::sample1);
                    f.complete();
                })),
                Future.future(f -> test.verify(() -> {
                    assertThrows(IllegalStateException.class, () -> errorClient.sample2(null));
                    f.complete();
                })),
                Future.future(f -> test.verify(() -> {
                    assertThrows(IllegalStateException.class, errorClient::sample3);
                    f.complete();
                }))
        )).onComplete(result -> {
            shutdownMockWebServer();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    @VxClient
    @Mapping("${root}:${port}")
    public interface CncClient {

        <T extends CncResponse> Future<T> sample1(CncRequest<T> request);

        <T extends CncResponse> Future<Pair<HttpStatus, T>> sample2(CncRequest<T> request);
    }

    @SuppressWarnings({"UnusedReturnValue", "rawtypes"})
    @VxClient
    @Mapping("${root}:${port}")
    public interface CncErrorClient {

        <T> Future<T> sample1();

        <T extends OtherResponse> Future<T> sample2(OtherRequest<T> request);

        <T extends Map> Future<Pair<HttpStatus, T>> sample3();
    }
}
