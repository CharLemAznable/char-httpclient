package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonReturnErrorTest;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReturnErrorTest extends CommonReturnErrorTest {

    @Test
    public void testError() {
        startMockWebServer();

        val ohLoader = OhFactory.ohLoader(reflectFactory());

        val httpClient = ohLoader.getClient(ErrorHttpClient.class);

        assertThrows(IllegalStateException.class, httpClient::sampleFuture);
        assertThrows(IllegalStateException.class, httpClient::sampleFutureT);
        assertThrows(IllegalStateException.class, httpClient::sampleList);
        assertThrows(IllegalStateException.class, httpClient::sampleListT);
        assertThrows(IllegalStateException.class, httpClient::sampleFutureList);
        assertThrows(IllegalStateException.class, httpClient::sampleFutureListT);
        assertThrows(IllegalStateException.class, httpClient::samplePair);
        assertThrows(IllegalStateException.class, httpClient::samplePairTU);
        assertThrows(IllegalStateException.class, httpClient::sampleFuturePair);
        assertThrows(IllegalStateException.class, httpClient::sampleFuturePairTU);
        assertThrows(IllegalStateException.class, httpClient::sampleTriple);
        assertThrows(IllegalStateException.class, httpClient::sampleTripleTUV);
        assertThrows(IllegalStateException.class, httpClient::sampleFutureTriple);
        assertThrows(IllegalStateException.class, httpClient::sampleFutureTripleTUV);

        assertThrows(IllegalArgumentException.class, httpClient::sampleMapError);
        val map = httpClient.sampleMap();
        assertEquals("Doe", map.get("John"));

        shutdownMockWebServer();
    }

    @SuppressWarnings({"UnusedReturnValue", "rawtypes"})
    @OhClient
    @Mapping("${root}:41196")
    public interface ErrorHttpClient {

        Future sampleFuture();

        <T> Future<T> sampleFutureT();

        List sampleList();

        <T> List<T> sampleListT();

        Future<List> sampleFutureList();

        <T> Future<List<T>> sampleFutureListT();

        Pair samplePair();

        <T, U> Pair<T, U> samplePairTU();

        Future<Pair> sampleFuturePair();

        <T, U> Future<Pair<T, U>> sampleFuturePairTU();

        Triple sampleTriple();

        <T, U, V> Triple<T, U, V> sampleTripleTUV();

        Future<Triple> sampleFutureTriple();

        <T, U, V> Future<Triple<T, U, V>> sampleFutureTripleTUV();

        Map sampleMapError();

        Map sampleMap();
    }
}
