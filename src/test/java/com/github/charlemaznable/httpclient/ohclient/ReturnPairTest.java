package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonReturnPairTest;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReturnPairTest extends CommonReturnPairTest {

    @SneakyThrows
    @Test
    public void testPair() {
        startMockWebServer();

        val ohLoader = OhFactory.ohLoader(reflectFactory());

        val httpClient = ohLoader.getClient(PairHttpClient.class);

        Pair<Integer, Bean> pair = httpClient.sampleStatusAndBean();
        assertEquals(HttpStatus.OK.value(), pair.getKey());
        assertEquals("John", pair.getValue().getName());
        val futurePair = httpClient.sampleFutureStatusAndBean();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futurePair::isDone);
        pair = futurePair.get();
        assertEquals(HttpStatus.OK.value(), pair.getKey());
        assertEquals("John", pair.getValue().getName());

        Pair<String, Bean> rawPair = httpClient.sampleRawAndBean();
        assertEquals(json(new Bean("Doe")), rawPair.getKey());
        assertEquals("Doe", rawPair.getValue().getName());
        val futureRawPair = httpClient.sampleFutureRawAndBean();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureRawPair::isDone);
        rawPair = futureRawPair.get();
        assertEquals(json(new Bean("Doe")), rawPair.getKey());
        assertEquals("Doe", rawPair.getValue().getName());

        shutdownMockWebServer();
    }

    @OhClient
    @Mapping("${root}:41194")
    public interface PairHttpClient {

        Pair<Integer, Bean> sampleStatusAndBean();

        Future<Pair<Integer, Bean>> sampleFutureStatusAndBean();

        Pair<String, Bean> sampleRawAndBean();

        Future<Pair<String, Bean>> sampleFutureRawAndBean();
    }
}
