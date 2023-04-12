package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonReturnTripleTest;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReturnTripleTest extends CommonReturnTripleTest {

    @SneakyThrows
    @Test
    public void testTriple() {
        startMockWebServer();

        val ohLoader = OhFactory.ohLoader(reflectFactory());

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

        shutdownMockWebServer();
    }

    @OhClient
    @Mapping("${root}:41195")
    public interface TripleHttpClient {

        Triple<Integer, HttpStatus, Bean> sampleStatusCodeAndBean();

        Future<Triple<Integer, HttpStatus, Bean>> sampleFutureStatusCodeAndBean();

        Triple<InputStream, String, Bean> sampleRawStreamAndBean();

        Future<Triple<InputStream, String, Bean>> sampleFutureRawStreamAndBean();
    }
}
