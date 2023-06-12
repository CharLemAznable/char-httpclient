package com.github.charlemaznable.httpclient.wfclient;

import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonReturnMapTest;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ReturnMapTest extends CommonReturnMapTest {

    @SuppressWarnings("rawtypes")
    @SneakyThrows
    @Test
    public void testMap() {
        startMockWebServer();

        val wfLoader = WfFactory.wfLoader(reflectFactory());

        val httpClient = wfLoader.getClient(MapHttpClient.class);

        Map<String, Object> map = requireNonNull(httpClient.sampleMap().block());
        Map beanMap = (Map) map.get("John");
        assertEquals("Doe", beanMap.get("name"));

        map = httpClient.sampleMapNull().block();
        assertNull(map);

        shutdownMockWebServer();
    }

    @WfClient
    @Mapping("${root}:41193")
    public interface MapHttpClient {

        Mono<Map<String, Object>> sampleMap();

        Mono<Map<String, Object>> sampleMapNull();
    }
}
