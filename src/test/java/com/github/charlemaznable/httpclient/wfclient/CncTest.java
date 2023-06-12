package com.github.charlemaznable.httpclient.wfclient;

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
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

        val wfLoader = WfFactory.wfLoader(reflectFactory());

        val client = wfLoader.getClient(CncClient.class);

        val response = client.sample1(new TestRequest()).block();
        assertNotNull(response);
        assertEquals(CONTENT, response.getContent());
        val nullResponse = client.sample1(null).block();
        assertTrue(nullResponse instanceof CncResponseImpl);

        val pair = client.sample2(new TestRequest()).block();
        assertNotNull(pair);
        assertEquals(HttpStatus.OK, pair.getLeft());
        assertEquals(CONTENT, pair.getRight().getContent());

        val errorClient = wfLoader.getClient(CncErrorClient.class);

        assertThrows(IllegalStateException.class, errorClient::sample1);
        //noinspection ReactiveStreamsUnusedPublisher
        assertThrows(IllegalStateException.class, () -> errorClient.sample2(null));
        assertThrows(IllegalStateException.class, errorClient::sample3);

        shutdownMockWebServer();
    }

    @WfClient
    @Mapping("${root}:${port}")
    public interface CncClient {

        <T extends CncResponse> Mono<T> sample1(CncRequest<T> request);

        <T extends CncResponse> Mono<Pair<HttpStatus, T>> sample2(CncRequest<T> request);
    }

    @SuppressWarnings({"UnusedReturnValue", "rawtypes"})
    @WfClient
    @Mapping("${root}:${port}")
    public interface CncErrorClient {

        <T> Mono<T> sample1();

        <T extends OtherResponse> Mono<T> sample2(OtherRequest<T> request);

        <T extends Map> Mono<Pair<HttpStatus, T>> sample3();
    }
}
