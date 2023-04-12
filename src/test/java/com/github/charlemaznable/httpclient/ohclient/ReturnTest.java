package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.annotation.DefaultFallbackDisabled;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonReturnTest;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.codec.Bytes.string;
import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReturnTest extends CommonReturnTest {

    @SneakyThrows
    @Test
    public void testStatusCode() {
        startMockWebServer1();

        val ohLoader = OhFactory.ohLoader(reflectFactory());

        val httpClient = ohLoader.getClient(StatusCodeHttpClient.class);

        assertDoesNotThrow(httpClient::sampleVoid);
        val futureVoid = httpClient.sampleFutureVoid();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureVoid::isDone);
        assertDoesNotThrow((ThrowingSupplier<Void>) futureVoid::get);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), httpClient.sampleStatusCode());
        val futureStatusCode = httpClient.sampleFutureStatusCode();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureStatusCode::isDone);
        assertEquals(HttpStatus.NOT_IMPLEMENTED.value(), futureStatusCode.get());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, httpClient.sampleStatus());
        val futureStatus = httpClient.sampleFutureStatus();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureStatus::isDone);
        assertEquals(HttpStatus.NOT_IMPLEMENTED, futureStatus.get());

        assertEquals(HttpStatus.Series.SERVER_ERROR, httpClient.sampleStatusSeries());
        val futureStatusSeries = httpClient.sampleFutureStatusSeries();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureStatusSeries::isDone);
        assertEquals(HttpStatus.Series.SERVER_ERROR, futureStatusSeries.get());

        assertTrue(httpClient.sampleSuccess());
        val futureFailure = httpClient.sampleFailure();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureFailure::isDone);
        assertFalse(futureFailure.get());

        shutdownMockWebServer1();
    }

    @SneakyThrows
    @Test
    public void testResponseBody() {
        startMockWebServer2();

        val ohLoader = OhFactory.ohLoader(reflectFactory());

        val httpClient = ohLoader.getClient(ResponseBodyHttpClient.class);

        assertNotNull(httpClient.sampleResponseBody());
        val futureResponseBody = httpClient.sampleFutureResponseBody();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureResponseBody::isDone);
        assertNotNull(futureResponseBody.get());

        @Cleanup val isr = new InputStreamReader(httpClient.sampleInputStream(), StandardCharsets.UTF_8);
        try (val bufferedReader = new BufferedReader(isr)) {
            assertEquals("OK", bufferedReader.readLine());
        }
        val futureInputStream = httpClient.sampleFutureInputStream();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureInputStream::isDone);
        @Cleanup val isr2 = new InputStreamReader(futureInputStream.get(), StandardCharsets.UTF_8);
        try (val bufferedReader = new BufferedReader(isr2)) {
            assertEquals("OK", bufferedReader.readLine());
        }

        assertEquals("OK", httpClient.sampleBufferedSource().readUtf8());
        val futureBufferedSource = httpClient.sampleFutureBufferedSource();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureBufferedSource::isDone);
        assertEquals("OK", futureBufferedSource.get().readUtf8());

        assertEquals("OK", string(httpClient.sampleByteArray()));
        val futureByteArray = httpClient.sampleFutureByteArray();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureByteArray::isDone);
        assertEquals("OK", string(futureByteArray.get()));

        try (val bufferedReader = new BufferedReader(httpClient.sampleReader())) {
            assertEquals("OK", bufferedReader.readLine());
        }
        val futureReader = httpClient.sampleFutureReader();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureReader::isDone);
        try (val bufferedReader = new BufferedReader(futureReader.get())) {
            assertEquals("OK", bufferedReader.readLine());
        }

        assertEquals("OK", httpClient.sampleString());
        val futureString = httpClient.sampleFutureString();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureString::isDone);
        assertEquals("OK", futureString.get());

        shutdownMockWebServer2();
    }

    @DefaultFallbackDisabled
    @OhClient
    @Mapping("${root}:41190")
    public interface StatusCodeHttpClient {

        void sampleVoid();

        Future<Void> sampleFutureVoid();

        int sampleStatusCode();

        Future<Integer> sampleFutureStatusCode();

        @Mapping("/sampleStatusCode")
        HttpStatus sampleStatus();

        @Mapping("/sampleFutureStatusCode")
        Future<HttpStatus> sampleFutureStatus();

        @Mapping("/sampleStatusCode")
        HttpStatus.Series sampleStatusSeries();

        @Mapping("/sampleFutureStatusCode")
        Future<HttpStatus.Series> sampleFutureStatusSeries();

        @Mapping("/sampleVoid")
        boolean sampleSuccess();

        @Mapping("/sampleStatusCode")
        Future<Boolean> sampleFailure();
    }

    @OhClient
    public interface ResponseBodyHttpClient {

        @TestMapping
        ResponseBody sampleResponseBody();

        @TestMapping
        Future<ResponseBody> sampleFutureResponseBody();

        @TestMapping
        InputStream sampleInputStream();

        @TestMapping
        Future<InputStream> sampleFutureInputStream();

        @TestMapping
        BufferedSource sampleBufferedSource();

        @TestMapping
        Future<BufferedSource> sampleFutureBufferedSource();

        @TestMapping
        byte[] sampleByteArray();

        @TestMapping
        Future<byte[]> sampleFutureByteArray();

        @TestMapping
        Reader sampleReader();

        @TestMapping
        Future<Reader> sampleFutureReader();

        @TestMapping
        String sampleString();

        @TestMapping
        Future<String> sampleFutureString();
    }
}
