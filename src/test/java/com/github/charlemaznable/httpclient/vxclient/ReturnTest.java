package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.httpclient.annotation.DefaultFallbackDisabled;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonReturnTest;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.VertxReflectFactory;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.github.charlemaznable.core.codec.Bytes.string;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(VertxExtension.class)
public class ReturnTest extends CommonReturnTest {

    @SneakyThrows
    @Test
    public void testStatusCode(Vertx vertx, VertxTestContext test) {
        startMockWebServer1();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));

        val httpClient = vxLoader.getClient(StatusCodeHttpClient.class);

        CompositeFuture.all(newArrayList(
                httpClient.sampleFutureVoid().onSuccess(result ->
                        test.verify(() -> assertNull(result))),
                httpClient.sampleFutureStatusCode().onSuccess(statusCode ->
                        test.verify(() -> assertEquals(HttpStatus.NOT_IMPLEMENTED.value(), statusCode))),
                httpClient.sampleFutureStatus().onSuccess(status ->
                        test.verify(() -> assertEquals(HttpStatus.NOT_IMPLEMENTED, status))),
                httpClient.sampleFutureStatusSeries().onSuccess(statusSeries ->
                        test.verify(() -> assertEquals(HttpStatus.Series.SERVER_ERROR, statusSeries))),
                httpClient.sampleFailure().onSuccess(failure ->
                        test.verify(() -> assertFalse(failure)))
        )).onComplete(result -> {
            shutdownMockWebServer1();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SneakyThrows
    @Test
    public void testResponseBody(Vertx vertx, VertxTestContext test) {
        startMockWebServer2();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));

        val httpClient = vxLoader.getClient(ResponseBodyHttpClient.class);

        CompositeFuture.all(newArrayList(
                httpClient.sampleFutureResponseBody().onSuccess(buffer ->
                        test.verify(() -> assertNotNull(buffer))),
                Future.future(f -> httpClient.sampleFutureByteArray()
                        .subscribe(bytes -> test.verify(() -> {
                            assertEquals("OK", string(bytes));
                            f.complete();
                        }))),
                Future.future(f -> httpClient.sampleFutureObject()
                        .subscribe(object -> test.verify(() -> {
                            assertEquals("Doe", object.getString("John"));
                            f.complete();
                        }))),
                Future.future(f -> httpClient.sampleFutureArray()
                        .subscribe(array -> test.verify(() -> {
                            assertEquals("John", array.getString(0));
                            assertEquals("Doe", array.getString(1));
                            f.complete();
                        })))
        )).onComplete(result -> {
            shutdownMockWebServer2();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    @DefaultFallbackDisabled
    @VxClient
    @Mapping("${root}:41190")
    public interface StatusCodeHttpClient {

        Future<Void> sampleFutureVoid();

        Future<Integer> sampleFutureStatusCode();

        @Mapping("/sampleFutureStatusCode")
        Future<HttpStatus> sampleFutureStatus();

        @Mapping("/sampleFutureStatusCode")
        Future<HttpStatus.Series> sampleFutureStatusSeries();

        @Mapping("/sampleStatusCode")
        Future<Boolean> sampleFailure();
    }

    @VxClient
    public interface ResponseBodyHttpClient {

        @TestMapping
        Future<Buffer> sampleFutureResponseBody();

        @TestMapping
        rx.Single<byte[]> sampleFutureByteArray();

        @Mapping("${root}:41191/sampleObject")
        io.reactivex.Single<JsonObject> sampleFutureObject();

        @Mapping("${root}:41191/sampleArray")
        io.reactivex.rxjava3.core.Single<JsonArray> sampleFutureArray();
    }
}
