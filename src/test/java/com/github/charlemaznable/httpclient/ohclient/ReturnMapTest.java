package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonReturnMapTest;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ReturnMapTest extends CommonReturnMapTest {

    @SuppressWarnings("rawtypes")
    @SneakyThrows
    @Test
    public void testMap() {
        startMockWebServer();

        val ohLoader = OhFactory.ohLoader(reflectFactory());

        val httpClient = ohLoader.getClient(MapHttpClient.class);

        Map<String, Object> map = httpClient.sampleMap();
        Map beanMap = (Map) map.get("John");
        assertEquals("Doe", beanMap.get("name"));

        Future<Map<String, Object>> futureMap = httpClient.sampleFutureMap();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureMap::isDone);
        map = futureMap.get();
        beanMap = (Map) map.get("John");
        assertEquals("Doe", beanMap.get("name"));

        map = httpClient.sampleMapNull();
        assertNull(map);

        futureMap = httpClient.sampleFutureMapNull();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureMap::isDone);
        map = futureMap.get();
        assertNull(map);

        shutdownMockWebServer();
    }

    @OhClient
    @Mapping("${root}:41193")
    public interface MapHttpClient {

        Map<String, Object> sampleMap();

        Future<Map<String, Object>> sampleFutureMap();

        Map<String, Object> sampleMapNull();

        Future<Map<String, Object>> sampleFutureMapNull();
    }
}
