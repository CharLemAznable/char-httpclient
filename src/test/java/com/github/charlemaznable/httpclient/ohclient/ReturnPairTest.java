package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.ohclient.OhFactory.OhLoader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReturnPairTest {

    private static OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @SneakyThrows
    @Test
    public void testPair() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    switch (request.getPath()) {
                        case "/sampleStatusAndBean":
                        case "/sampleFutureStatusAndBean":
                            return new MockResponse().setResponseCode(HttpStatus.OK.value())
                                    .setBody(json(new Bean("John")));
                        case "/sampleRawAndBean":
                        case "/sampleFutureRawAndBean":
                            return new MockResponse().setResponseCode(HttpStatus.OK.value())
                                    .setBody(json(new Bean("Doe")));
                        default:
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            });
            mockWebServer.start(41194);
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
        }
    }

    @OhClient
    @Mapping("${root}:41194")
    public interface PairHttpClient {

        Pair<Integer, Bean> sampleStatusAndBean();

        Future<Pair<Integer, Bean>> sampleFutureStatusAndBean();

        Pair<String, Bean> sampleRawAndBean();

        Future<Pair<String, Bean>> sampleFutureRawAndBean();
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class Bean {

        private String name;
    }
}
