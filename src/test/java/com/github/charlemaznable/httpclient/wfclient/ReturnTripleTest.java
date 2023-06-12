package com.github.charlemaznable.httpclient.wfclient;

import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonReturnTripleTest;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static com.github.charlemaznable.core.codec.Bytes.string;
import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReturnTripleTest extends CommonReturnTripleTest {

    @SneakyThrows
    @Test
    public void testTriple() {
        startMockWebServer();

        val wfLoader = WfFactory.wfLoader(reflectFactory());

        val httpClient = wfLoader.getClient(TripleHttpClient.class);

        Triple<Integer, HttpStatus, Bean> triple = requireNonNull(httpClient.sampleStatusCodeAndBean().block());
        assertEquals(HttpStatus.OK.value(), triple.getLeft());
        assertEquals(HttpStatus.OK, triple.getMiddle());
        assertEquals("John", triple.getRight().getName());

        Triple<byte[], String, Bean> rawTriple = requireNonNull(httpClient.sampleRawBytesAndBean().block());
        assertEquals(json(new Bean("Doe")), string(rawTriple.getLeft()));
        assertEquals(json(new Bean("Doe")), rawTriple.getMiddle());
        assertEquals("Doe", rawTriple.getRight().getName());

        shutdownMockWebServer();
    }

    @WfClient
    @Mapping("${root}:41195")
    public interface TripleHttpClient {

        Mono<Triple<Integer, HttpStatus, Bean>> sampleStatusCodeAndBean();

        @Mapping("/sampleRawStreamAndBean")
        Mono<Triple<byte[], String, Bean>> sampleRawBytesAndBean();
    }
}
