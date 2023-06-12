package com.github.charlemaznable.httpclient.wfclient;

import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonReturnErrorTest;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReturnErrorTest extends CommonReturnErrorTest {

    @Test
    public void testError() {
        startMockWebServer();

        val wfLoader = WfFactory.wfLoader(reflectFactory());

        val httpClient = wfLoader.getClient(ErrorHttpClient.class);

        assertThrows(IllegalStateException.class, httpClient::sampleObject);
        assertThrows(IllegalStateException.class, httpClient::sampleFuture);
        assertThrows(IllegalStateException.class, httpClient::sampleFutureT);
        assertThrows(IllegalStateException.class, httpClient::sampleFutureList);
        assertThrows(IllegalStateException.class, httpClient::sampleFutureListT);
        assertThrows(IllegalStateException.class, httpClient::sampleFuturePair);
        assertThrows(IllegalStateException.class, httpClient::sampleFuturePairTU);
        assertThrows(IllegalStateException.class, httpClient::sampleFutureTriple);
        assertThrows(IllegalStateException.class, httpClient::sampleFutureTripleTUV);

        assertThrows(IllegalArgumentException.class, () -> httpClient.sampleMapError().block());
        val map = requireNonNull(httpClient.sampleMap().block());
        assertEquals("Doe", map.get("John"));

        shutdownMockWebServer();
    }

    @SuppressWarnings({"UnusedReturnValue", "rawtypes"})
    @WfClient
    @Mapping("${root}:41196")
    public interface ErrorHttpClient {

        Object sampleObject();

        Mono sampleFuture();

        <T> Mono<T> sampleFutureT();

        Mono<List> sampleFutureList();

        <T> Mono<List<T>> sampleFutureListT();

        Mono<Pair> sampleFuturePair();

        <T, U> Mono<Pair<T, U>> sampleFuturePairTU();

        Mono<Triple> sampleFutureTriple();

        <T, U, V> Mono<Triple<T, U, V>> sampleFutureTripleTUV();

        Mono<Map> sampleMapError();

        Mono<Map> sampleMap();
    }
}
