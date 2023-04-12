package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.core.config.Arguments;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CncRequest;
import com.github.charlemaznable.httpclient.common.CncResponse;
import com.github.charlemaznable.httpclient.common.CncResponse.CncResponseImpl;
import com.github.charlemaznable.httpclient.common.CommonCncTest;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CncTest extends CommonCncTest {

    @BeforeAll
    public static void beforeAll() {
        Arguments.initial("--port=41200");
    }

    @AfterAll
    public static void afterAll() {
        Arguments.initial();
    }

    @SneakyThrows
    @Test
    public void testCncClient() {
        startMockWebServer();

        val ohLoader = OhFactory.ohLoader(reflectFactory());

        val client = ohLoader.getClient(CncClient.class);

        val response = client.sample1(new TestRequest());
        assertEquals(CONTENT, response.getContent());
        val nullResponse = client.sample1(null);
        assertTrue(nullResponse instanceof CncResponseImpl);

        val futureResponse = client.sample2(new TestRequest());
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureResponse::isDone);
        assertEquals(CONTENT, futureResponse.get().getContent());

        val pair = client.sample3(new TestRequest());
        assertEquals(HttpStatus.OK, pair.getLeft());
        assertEquals(CONTENT, pair.getRight().getContent());

        val futurePair = client.sample4(new TestRequest());
        await().forever().pollDelay(Duration.ofMillis(100)).until(futurePair::isDone);
        assertEquals(HttpStatus.OK, futurePair.get().getLeft());
        assertEquals(CONTENT, futurePair.get().getRight().getContent());

        val errorClient = ohLoader.getClient(CncErrorClient.class);

        assertThrows(IllegalStateException.class, errorClient::sample1);
        assertThrows(IllegalStateException.class, () -> errorClient.sample2(null));
        assertThrows(IllegalStateException.class, errorClient::sample3);

        shutdownMockWebServer();
    }

    @OhClient
    @Mapping("${root}:${port}")
    public interface CncClient {

        <T extends CncResponse> T sample1(CncRequest<T> request);

        <T extends CncResponse> Future<T> sample2(CncRequest<T> request);

        <T extends CncResponse> Pair<HttpStatus, T> sample3(CncRequest<T> request);

        <T extends CncResponse> Future<Pair<HttpStatus, T>> sample4(CncRequest<T> request);
    }

    @SuppressWarnings({"UnusedReturnValue", "rawtypes"})
    @OhClient
    @Mapping("${root}:${port}")
    public interface CncErrorClient {

        <T> T sample1();

        <T extends OtherResponse> Future<T> sample2(OtherRequest<T> request);

        <T extends Map> Pair<HttpStatus, T> sample3();
    }
}
