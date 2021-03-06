package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.ohclient.OhFactory.OhLoader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.codec.Xml.xml;
import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Mapp.of;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ReturnMapTest {

    private static OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @SneakyThrows
    @Test
    public void testMap() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    switch (request.getPath()) {
                        case "/sampleMap":
                            return new MockResponse().setResponseCode(HttpStatus.OK.value())
                                    .setBody(json(of("John", of("name", "Doe"))));
                        case "/sampleFutureMap":
                            return new MockResponse().setResponseCode(HttpStatus.OK.value())
                                    .setBody(xml(of("John", of("name", "Doe"))));
                        case "/sampleMapNull":
                        case "/sampleFutureMapNull":
                            return new MockResponse().setResponseCode(HttpStatus.OK.value())
                                    .setBody("");
                        default:
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            });
            mockWebServer.start(41193);
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
        }
    }

    @OhClient
    @Mapping("${root}:41193")
    public interface MapHttpClient {

        Map<String, Object> sampleMap();

        Future<Map<String, Object>> sampleFutureMap();

        Map<String, Object> sampleMapNull();

        Future<Map<String, Object>> sampleFutureMapNull();
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class Bean {

        private String name;
    }
}
