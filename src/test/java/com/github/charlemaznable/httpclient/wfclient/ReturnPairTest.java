package com.github.charlemaznable.httpclient.wfclient;

import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonReturnPairTest;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReturnPairTest extends CommonReturnPairTest {

    @SneakyThrows
    @Test
    public void testPair() {
        startMockWebServer();

        val wfLoader = WfFactory.wfLoader(reflectFactory());

        val httpClient = wfLoader.getClient(PairHttpClient.class);

        Pair<Integer, Bean> pair = requireNonNull(httpClient.sampleStatusAndBean().block());
        assertEquals(HttpStatus.OK.value(), pair.getKey());
        assertEquals("John", pair.getValue().getName());

        Pair<String, Bean> rawPair = requireNonNull(httpClient.sampleRawAndBean().block());
        assertEquals(json(new Bean("Doe")), rawPair.getKey());
        assertEquals("Doe", rawPair.getValue().getName());

        shutdownMockWebServer();
    }

    @WfClient
    @Mapping("${root}:41194")
    public interface PairHttpClient {

        Mono<Pair<Integer, Bean>> sampleStatusAndBean();

        Mono<Pair<String, Bean>> sampleRawAndBean();
    }
}
