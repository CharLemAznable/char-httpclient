package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.httpclient.common.CommonReqTest;
import com.github.charlemaznable.httpclient.annotation.ContentFormat;
import com.github.charlemaznable.httpclient.common.FallbackFunction;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.StatusError;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import lombok.val;

import java.net.ConnectException;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.of;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class VxReqCommonTest extends CommonReqTest {

    public void testVxReq(Vertx vertx, VertxTestContext test) {
        startMockWebServer(9300);

        val instance = new VxReq(vertx, "http://127.0.0.1:9300").buildInstance();

        CompositeFuture.all(newArrayList(
                Future.<String>future(f ->
                        new VxReq(vertx, "http://127.0.0.1:9300/sample1")
                                .acceptCharset(ISO_8859_1)
                                .contentFormat(new ContentFormat.FormContentFormatter())
                                .header("AAA", "aaa")
                                .headers(of("AAA", null, "BBB", "bbb"))
                                .parameter("CCC", "ccc")
                                .get(async -> test.verify(() ->
                                        assertEquals("Sample1", async.result())), f)),
                Future.<String>future(f ->
                        new VxReq(vertx).req("http://127.0.0.1:9300/sample2")
                                .parameter("AAA", "aaa")
                                .parameters(of("AAA", null, "BBB", "bbb"))
                                .post(async -> test.verify(() ->
                                        assertEquals("Sample2", async.result())), f)),
                Future.<String>future(f ->
                        new VxReq(vertx, "http://127.0.0.1:9300")
                                .req("/sample3?DDD=ddd")
                                .parameter("AAA", "aaa")
                                .parameters(of("AAA", null, "BBB", "bbb"))
                                .requestBody("CCC=ccc")
                                .get()
                                .onComplete(async -> test.verify(() ->
                                        assertEquals("Sample3", async.result())))
                                .andThen(f)),
                Future.<String>future(f ->
                        new VxReq(vertx, "http://127.0.0.1:9300")
                                .req("/sample4")
                                .parameter("AAA", "aaa")
                                .parameters(of("AAA", null, "BBB", "bbb"))
                                .requestBody("CCC=ccc")
                                .post()
                                .onComplete(async -> test.verify(() ->
                                        assertEquals("Sample4", async.result())))
                                .andThen(f)),
                Future.<String>future(f ->
                        new VxReq(vertx, "http://127.0.0.1:9300/sample5")
                                .get(async -> test.verify(() -> {
                                    assertTrue(async.cause() instanceof StatusError);
                                    StatusError e = (StatusError) async.cause();
                                    assertEquals(HttpStatus.NOT_FOUND.value(), e.getStatusCode());
                                    assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), e.getMessage());
                                    f.complete();
                                }))),
                Future.<String>future(f ->
                        new VxReq(vertx, "http://127.0.0.1:9300/sample5")
                                .parameter("AAA", "aaa")
                                .get(async -> test.verify(() -> {
                                    assertTrue(async.cause() instanceof StatusError);
                                    StatusError e = (StatusError) async.cause();
                                    assertEquals(HttpStatus.FORBIDDEN.value(), e.getStatusCode());
                                    assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), e.getMessage());
                                    f.complete();
                                }))),
                Future.<String>future(f ->
                        new VxReq(vertx, "http://127.0.0.1:9300/sample6")
                                .statusFallback(HttpStatus.NOT_FOUND, new NotFound())
                                .statusSeriesFallback(HttpStatus.Series.CLIENT_ERROR, new ClientError())
                                .get(async -> test.verify(() ->
                                        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), async.result())), f)),
                Future.<String>future(f ->
                        new VxReq(vertx, "http://127.0.0.1:9300/sample6")
                                .parameter("AAA", "aaa")
                                .statusFallback(HttpStatus.NOT_FOUND, new NotFound())
                                .statusSeriesFallback(HttpStatus.Series.CLIENT_ERROR, new ClientError())
                                .get(async -> test.verify(() ->
                                        assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), async.result())), f)),
                Future.<String>future(f ->
                        new VxReq(vertx, "http://127.0.0.1:9300/sample7")
                                .contentFormat(new ContentFormat.JsonContentFormatter())
                                .parameter("BBB", "bbb")
                                .extraUrlQueryBuilder((parameterMap, contextMap) -> "AAA=aaa")
                                .get(async -> test.verify(() ->
                                        assertEquals("Sample7", async.result())), f)),
                Future.<String>future(f ->
                        new VxReq(vertx, "http://127.0.0.1:9300/sample7")
                                .contentFormat(new ContentFormat.JsonContentFormatter())
                                .parameter("BBB", "bbb")
                                .extraUrlQueryBuilder((parameterMap, contextMap) -> "AAA=aaa")
                                .post(async -> test.verify(() ->
                                        assertEquals("Sample7", async.result())), f)),
                Future.<String>future(f ->
                        new VxReq(vertx, "http://127.0.0.1:9399/error")
                                .get(async -> test.verify(() -> {
                                    assertTrue(async.cause() instanceof ConnectException);
                                    f.complete();
                                }), null)),
                Future.<String>future(f ->
                        instance.req("/sample1")
                                .acceptCharset(ISO_8859_1)
                                .contentFormat(new ContentFormat.FormContentFormatter())
                                .header("AAA", "aaa")
                                .headers(of("AAA", null, "BBB", "bbb"))
                                .parameter("CCC", "ccc")
                                .get(async -> test.verify(() ->
                                        assertEquals("Sample1", async.result())), f)),
                Future.<String>future(f ->
                        instance.req("/sample2")
                                .parameter("AAA", "aaa")
                                .parameters(of("AAA", null, "BBB", "bbb"))
                                .post(async -> test.verify(() ->
                                        assertEquals("Sample2", async.result())), f)),
                Future.<String>future(f ->
                        instance.req("/sample3?DDD=ddd")
                                .parameter("AAA", "aaa")
                                .parameters(of("AAA", null, "BBB", "bbb"))
                                .requestBody("CCC=ccc")
                                .get()
                                .onComplete(async -> test.verify(() ->
                                        assertEquals("Sample3", async.result())))
                                .andThen(f)),
                Future.<String>future(f ->
                        instance.req("/sample4")
                                .parameter("AAA", "aaa")
                                .parameters(of("AAA", null, "BBB", "bbb"))
                                .requestBody("CCC=ccc")
                                .post()
                                .onComplete(async -> test.verify(() ->
                                        assertEquals("Sample4", async.result())))
                                .andThen(f)),
                Future.<String>future(f ->
                        instance.req("/sample5")
                                .get(async -> test.verify(() -> {
                                    assertTrue(async.cause() instanceof StatusError);
                                    StatusError e = (StatusError) async.cause();
                                    assertEquals(HttpStatus.NOT_FOUND.value(), e.getStatusCode());
                                    assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), e.getMessage());
                                    f.complete();
                                }))),
                Future.<String>future(f ->
                        instance.req("/sample5")
                                .parameter("AAA", "aaa")
                                .get(async -> test.verify(() -> {
                                    assertTrue(async.cause() instanceof StatusError);
                                    StatusError e = (StatusError) async.cause();
                                    assertEquals(HttpStatus.FORBIDDEN.value(), e.getStatusCode());
                                    assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), e.getMessage());
                                    f.complete();
                                }))),
                Future.<String>future(f ->
                        instance.req("/sample6")
                                .statusFallback(HttpStatus.NOT_FOUND, new NotFound())
                                .statusSeriesFallback(HttpStatus.Series.CLIENT_ERROR, new ClientError())
                                .get(async -> test.verify(() ->
                                        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), async.result())), f)),
                Future.<String>future(f ->
                        instance.req("/sample6")
                                .parameter("AAA", "aaa")
                                .statusFallback(HttpStatus.NOT_FOUND, new NotFound())
                                .statusSeriesFallback(HttpStatus.Series.CLIENT_ERROR, new ClientError())
                                .get(async -> test.verify(() ->
                                        assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), async.result())), f)),
                Future.<String>future(f ->
                        instance.req("/sample7")
                                .contentFormat(new ContentFormat.JsonContentFormatter())
                                .parameter("BBB", "bbb")
                                .extraUrlQueryBuilder((parameterMap, contextMap) -> "AAA=aaa")
                                .get(async -> test.verify(() ->
                                        assertEquals("Sample7", async.result())), f)),
                Future.<String>future(f ->
                        instance.req("/sample7")
                                .contentFormat(new ContentFormat.JsonContentFormatter())
                                .parameter("BBB", "bbb")
                                .extraUrlQueryBuilder((parameterMap, contextMap) -> "AAA=aaa")
                                .post(async -> test.verify(() ->
                                        assertEquals("Sample7", async.result())), f))
        )).onComplete(result -> {
            shutdownMockWebServer();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    public static class NotFound implements FallbackFunction<String> {

        @Override
        public String apply(Response response) {
            return response.responseBodyAsString();
        }
    }

    public static class ClientError implements FallbackFunction<String> {

        @Override
        public String apply(Response response) {
            return response.responseBodyAsString();
        }
    }
}
