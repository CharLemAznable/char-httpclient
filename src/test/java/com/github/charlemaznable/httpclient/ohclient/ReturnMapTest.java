package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonReturnMapTest;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
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

        Future<HashMap<String, Object>> futureHashMap = httpClient.sampleFutureMap();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureHashMap::isDone);
        map = futureHashMap.get();
        beanMap = (Map) map.get("John");
        assertEquals("Doe", beanMap.get("name"));

        map = httpClient.sampleMapNull();
        assertNull(map);

        Future<Map<String, Object>> futureMap = httpClient.sampleFutureMapNull();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureMap::isDone);
        map = futureMap.get();
        assertNull(map);

        shutdownMockWebServer();
    }

    @OhClient
    @Mapping("${root}:41193")
    public interface MapHttpClient {

        Map<String, Object> sampleMap();

        Future<HashMap<String, Object>> sampleFutureMap();

        Map<String, Object> sampleMapNull();

        Future<Map<String, Object>> sampleFutureMapNull();
    }
}
