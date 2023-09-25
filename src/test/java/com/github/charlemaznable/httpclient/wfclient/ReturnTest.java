package com.github.charlemaznable.httpclient.wfclient;

import com.github.charlemaznable.httpclient.annotation.DefaultFallbackDisabled;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonResponse;
import com.github.charlemaznable.httpclient.common.CommonReturnTest;
import com.github.charlemaznable.httpclient.common.HttpHeaders;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import io.smallrye.mutiny.Uni;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.codec.Bytes.string;
import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static java.util.Objects.requireNonNull;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReturnTest extends CommonReturnTest {

    @SneakyThrows
    @Test
    public void testStatusCode() {
        startMockWebServer1();

        val wfLoader = WfFactory.wfLoader(reflectFactory());

        val httpClient = wfLoader.getClient(StatusCodeHttpClient.class);

        assertDoesNotThrow(() -> httpClient.sampleVoid().block());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), httpClient.sampleStatusCode().block());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, httpClient.sampleStatus().block());

        assertEquals(HttpStatus.Series.SERVER_ERROR, httpClient.sampleStatusSeries().block());

        assertTrue(requireNonNull(httpClient.sampleSuccess().block()));

        shutdownMockWebServer1();
    }

    @SneakyThrows
    @Test
    public void testResponseBody() {
        startMockWebServer2();

        val wfLoader = WfFactory.wfLoader(reflectFactory());

        val httpClient = wfLoader.getClient(ResponseBodyHttpClient.class);

        val futureByteArray = httpClient.sampleByteArray();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureByteArray::isDone);
        assertEquals("OK", string(futureByteArray.get()));

        val futureString = httpClient.sampleString().subscribeAsCompletionStage();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureString::isDone);
        assertEquals("OK", futureString.get());

        val httpHeaders = httpClient.sampleFutureHttpHeaders().block();
        assertNotNull(httpHeaders);
        assertEquals("OK", httpHeaders.get("Custom-Header").get(0));

        val commonResponse = httpClient.sampleFutureCommonResponse().block();
        assertNotNull(commonResponse);
        assertEquals("OK", commonResponse.getHeaders().get("custom-header").get(0));
        assertEquals("OK", commonResponse.getBody());

        shutdownMockWebServer2();
    }

    @DefaultFallbackDisabled
    @WfClient
    @Mapping("${root}:41190")
    public interface StatusCodeHttpClient {

        Mono<Void> sampleVoid();

        Mono<Integer> sampleStatusCode();

        @Mapping("/sampleStatusCode")
        Mono<HttpStatus> sampleStatus();

        @Mapping("/sampleStatusCode")
        Mono<HttpStatus.Series> sampleStatusSeries();

        @Mapping("/sampleVoid")
        Mono<Boolean> sampleSuccess();
    }

    @WfClient
    public interface ResponseBodyHttpClient {

        @TestMapping
        Future<byte[]> sampleByteArray();

        @TestMapping
        Uni<String> sampleString();

        @TestMapping
        Mono<HttpHeaders> sampleFutureHttpHeaders();

        @TestMapping
        Mono<CommonResponse> sampleFutureCommonResponse();
    }
}
