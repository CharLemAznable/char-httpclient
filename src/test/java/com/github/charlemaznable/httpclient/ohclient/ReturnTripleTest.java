package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.ohclient.OhFactory.OhLoader;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static java.util.Objects.requireNonNull;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReturnTripleTest {

    private static final OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @SneakyThrows
    @Test
    public void testTriple() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    return switch (requireNonNull(request.getPath())) {
                        case "/sampleStatusCodeAndBean", "/sampleFutureStatusCodeAndBean" -> new MockResponse()
                                .setResponseCode(HttpStatus.OK.value())
                                .setBody(json(new Bean("John")));
                        case "/sampleRawStreamAndBean", "/sampleFutureRawStreamAndBean" -> new MockResponse()
                                .setResponseCode(HttpStatus.OK.value())
                                .setBody(json(new Bean("Doe")));
                        default -> new MockResponse()
                                .setResponseCode(HttpStatus.NOT_FOUND.value())
                                .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    };
                }
            });
            mockWebServer.start(41195);
            val httpClient = ohLoader.getClient(TripleHttpClient.class);

            Triple<Integer, HttpStatus, Bean> triple = httpClient.sampleStatusCodeAndBean();
            assertEquals(HttpStatus.OK.value(), triple.getLeft());
            assertEquals(HttpStatus.OK, triple.getMiddle());
            assertEquals("John", triple.getRight().getName());
            val futureTriple = httpClient.sampleFutureStatusCodeAndBean();
            await().forever().pollDelay(Duration.ofMillis(100)).until(futureTriple::isDone);
            triple = futureTriple.get();
            assertEquals(HttpStatus.OK.value(), triple.getLeft());
            assertEquals(HttpStatus.OK, triple.getMiddle());
            assertEquals("John", triple.getRight().getName());

            Triple<InputStream, String, Bean> rawTriple = httpClient.sampleRawStreamAndBean();
            @Cleanup val isr1 = new InputStreamReader(rawTriple.getLeft(), StandardCharsets.UTF_8);
            try (val bufferedReader = new BufferedReader(isr1)) {
                assertEquals(json(new Bean("Doe")), bufferedReader.readLine());
            }
            assertEquals(json(new Bean("Doe")), rawTriple.getMiddle());
            assertEquals("Doe", rawTriple.getRight().getName());
            val futureRawTriple = httpClient.sampleFutureRawStreamAndBean();
            await().forever().pollDelay(Duration.ofMillis(100)).until(futureRawTriple::isDone);
            rawTriple = futureRawTriple.get();
            @Cleanup val isr2 = new InputStreamReader(rawTriple.getLeft(), StandardCharsets.UTF_8);
            try (val bufferedReader = new BufferedReader(isr2)) {
                assertEquals(json(new Bean("Doe")), bufferedReader.readLine());
            }
            assertEquals(json(new Bean("Doe")), rawTriple.getMiddle());
            assertEquals("Doe", rawTriple.getRight().getName());
        }
    }

    @OhClient
    @Mapping("${root}:41195")
    public interface TripleHttpClient {

        Triple<Integer, HttpStatus, Bean> sampleStatusCodeAndBean();

        Future<Triple<Integer, HttpStatus, Bean>> sampleFutureStatusCodeAndBean();

        Triple<InputStream, String, Bean> sampleRawStreamAndBean();

        Future<Triple<InputStream, String, Bean>> sampleFutureRawStreamAndBean();
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class Bean {

        private String name;
    }
}
