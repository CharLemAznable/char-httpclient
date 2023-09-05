package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.DefaultFallbackDisabled;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.StatusFallback;
import com.github.charlemaznable.httpclient.annotation.StatusSeriesFallback;
import com.github.charlemaznable.httpclient.common.CommonFallbackTest;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.StatusError;
import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class FallbackTest extends CommonFallbackTest {

    @Test
    public void testFallback(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));

        val httpClient = vxLoader.getClient(MappingHttpClient.class);
        val defaultHttpClient = vxLoader.getClient(DefaultMappingHttpClient.class);
        val disabledHttpClient = vxLoader.getClient(DisabledMappingHttpClient.class);
        val httpClientNeo = vxLoader.getClient(MappingHttpClientNeo.class);
        val disabledHttpClientNeo = vxLoader.getClient(DisabledMappingHttpClientNeo.class);

        Future.all(newArrayList(
                Future.all(newArrayList(
                        httpClient.sampleNotFound().onSuccess(response -> test.verify(() -> assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), response))),
                        httpClient.sampleClientError().onSuccess(response -> test.verify(() -> assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), response))),
                        httpClient.sampleMappingNotFound().onSuccess(response -> test.verify(() -> assertEquals("\"" + HttpStatus.NOT_FOUND.getReasonPhrase() + "\"", response))),
                        httpClient.sampleMappingClientError().onSuccess(response -> test.verify(() -> assertEquals("\"" + HttpStatus.FORBIDDEN.getReasonPhrase() + "\"", response))),
                        Future.future(f -> httpClient.sampleServerError().onFailure(ex -> {
                            test.verify(() -> assertTrue(ex instanceof StatusError));
                            f.complete();
                        }))
                )),
                Future.all(newArrayList(
                        Future.future(f -> defaultHttpClient.sampleNotFound().onFailure(ex -> {
                            test.verify(() -> {
                                assertTrue(ex instanceof StatusError);
                                StatusError err = (StatusError) ex;
                                assertEquals(HttpStatus.NOT_FOUND.value(), err.getStatusCode());
                                assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), err.getMessage());
                            });
                            f.complete();
                        })),
                        Future.future(f -> defaultHttpClient.sampleClientError().onFailure(ex -> {
                            test.verify(() -> {
                                assertTrue(ex instanceof StatusError);
                                StatusError err = (StatusError) ex;
                                assertEquals(HttpStatus.FORBIDDEN.value(), err.getStatusCode());
                                assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), err.getMessage());
                            });
                            f.complete();
                        })),
                        Future.future(f -> defaultHttpClient.sampleMappingNotFound().onFailure(ex -> {
                            test.verify(() -> {
                                assertTrue(ex instanceof StatusError);
                                StatusError err = (StatusError) ex;
                                assertEquals(HttpStatus.NOT_FOUND.value(), err.getStatusCode());
                                assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), err.getMessage());
                            });
                            f.complete();
                        })),
                        Future.future(f -> defaultHttpClient.sampleMappingClientError().onFailure(ex -> {
                            test.verify(() -> {
                                assertTrue(ex instanceof StatusError);
                                StatusError err = (StatusError) ex;
                                assertEquals(HttpStatus.FORBIDDEN.value(), err.getStatusCode());
                                assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), err.getMessage());
                            });
                            f.complete();
                        })),
                        Future.future(f -> defaultHttpClient.sampleServerError().onFailure(ex -> {
                            test.verify(() -> {
                                assertTrue(ex instanceof StatusError);
                                StatusError err = (StatusError) ex;
                                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), err.getStatusCode());
                                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), err.getMessage());
                            });
                            f.complete();
                        }))
                )),
                Future.all(newArrayList(
                        disabledHttpClient.sampleNotFound().onSuccess(response -> test.verify(() -> assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), response))),
                        disabledHttpClient.sampleClientError().onSuccess(response -> test.verify(() -> assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), response))),
                        disabledHttpClient.sampleMappingNotFound().onSuccess(response -> test.verify(() -> assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), response))),
                        disabledHttpClient.sampleMappingClientError().onSuccess(response -> test.verify(() -> assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), response))),
                        disabledHttpClient.sampleServerError().onSuccess(response -> test.verify(() -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), response)))
                )),
                Future.all(newArrayList(
                        httpClientNeo.sampleNotFound().onSuccess(response -> test.verify(() -> assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), response))),
                        httpClientNeo.sampleClientError().onSuccess(response -> test.verify(() -> assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), response))),
                        httpClientNeo.sampleMappingNotFound().onSuccess(response -> test.verify(() -> assertEquals("\"" + HttpStatus.NOT_FOUND.getReasonPhrase() + "\"", response))),
                        httpClientNeo.sampleMappingClientError().onSuccess(response -> test.verify(() -> assertEquals("\"" + HttpStatus.FORBIDDEN.getReasonPhrase() + "\"", response))),
                        Future.future(f -> httpClientNeo.sampleServerError().onFailure(ex -> {
                            test.verify(() -> assertTrue(ex instanceof StatusError));
                            f.complete();
                        }))
                )),
                Future.all(newArrayList(
                        disabledHttpClientNeo.sampleNotFound().onSuccess(response -> test.verify(() -> assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), response))),
                        disabledHttpClientNeo.sampleClientError().onSuccess(response -> test.verify(() -> assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), response))),
                        disabledHttpClientNeo.sampleMappingNotFound().onSuccess(response -> test.verify(() -> assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), response))),
                        disabledHttpClientNeo.sampleMappingClientError().onSuccess(response -> test.verify(() -> assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), response))),
                        disabledHttpClientNeo.sampleServerError().onSuccess(response -> test.verify(() -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), response)))
                ))
        )).onComplete(result -> {
            shutdownMockWebServer();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    @StatusFallback(status = HttpStatus.NOT_FOUND, fallback = NotFound.class)
    @StatusSeriesFallback(statusSeries = HttpStatus.Series.CLIENT_ERROR, fallback = ClientError.class)
    @Mapping("${root}:41180")
    @VxClient
    public interface MappingHttpClient {

        Future<String> sampleNotFound();

        Future<String> sampleClientError();

        @StatusFallback(status = HttpStatus.NOT_FOUND, fallback = NotFound2.class)
        @StatusSeriesFallback(statusSeries = HttpStatus.Series.CLIENT_ERROR, fallback = ClientError2.class)
        Future<String> sampleMappingNotFound();

        @StatusFallback(status = HttpStatus.NOT_FOUND, fallback = NotFound2.class)
        @StatusSeriesFallback(statusSeries = HttpStatus.Series.CLIENT_ERROR, fallback = ClientError2.class)
        Future<String> sampleMappingClientError();

        Future<String> sampleServerError();
    }

    @Mapping("${root}:41180")
    @VxClient
    public interface DefaultMappingHttpClient {

        Future<Void> sampleNotFound();

        Future<Void> sampleClientError();

        Future<Void> sampleMappingNotFound();

        Future<Void> sampleMappingClientError();

        Future<Void> sampleServerError();
    }

    @DefaultFallbackDisabled
    @Mapping("${root}:41180")
    @VxClient
    public interface DisabledMappingHttpClient {

        Future<String> sampleNotFound();

        Future<String> sampleClientError();

        Future<String> sampleMappingNotFound();

        Future<String> sampleMappingClientError();

        Future<String> sampleServerError();
    }

    @Mapping("${root}:41180")
    @VxClient
    @ConfigureWith(MappingHttpClientConfig.class)
    public interface MappingHttpClientNeo {

        Future<String> sampleNotFound();

        Future<String> sampleClientError();

        @ConfigureWith(SampleMappingConfig.class)
        Future<String> sampleMappingNotFound();

        @ConfigureWith(SampleMappingConfig.class)
        Future<String> sampleMappingClientError();

        Future<String> sampleServerError();
    }

    @Mapping("${root}:41180")
    @VxClient
    @ConfigureWith(DisabledMappingHttpClientConfig.class)
    public interface DisabledMappingHttpClientNeo {

        Future<String> sampleNotFound();

        Future<String> sampleClientError();

        Future<String> sampleMappingNotFound();

        Future<String> sampleMappingClientError();

        Future<String> sampleServerError();
    }
}
