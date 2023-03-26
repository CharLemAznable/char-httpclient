package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.ohclient.OhFactory.OhLoader;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.codec.Json.jsonOf;
import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReturnErrorTest {

    private static final OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @SneakyThrows
    @Test
    public void testError() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    return switch (requireNonNull(request.getPath())) {
                        case "/sampleFuture", "/sampleList" -> new MockResponse()
                                .setResponseCode(HttpStatus.OK.value())
                                .setBody(json(newArrayList("John", "Doe")));
                        case "/sampleMapError" -> new MockResponse()
                                .setResponseCode(HttpStatus.OK.value())
                                .setBody("John Doe");
                        case "/sampleMap", "/samplePair", "/sampleTriple" -> new MockResponse()
                                .setResponseCode(HttpStatus.OK.value())
                                .setBody(jsonOf("John", "Doe"));
                        default -> new MockResponse()
                                .setResponseCode(HttpStatus.NOT_FOUND.value())
                                .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    };
                }
            });
            mockWebServer.start(41196);
            val httpClient = ohLoader.getClient(ErrorHttpClient.class);

            assertThrows(IllegalStateException.class, httpClient::sampleFuture);
            assertThrows(IllegalStateException.class, httpClient::sampleList);
            assertThrows(IllegalArgumentException.class, httpClient::sampleMapError);

            val map = httpClient.sampleMap();
            assertEquals("Doe", map.get("John"));

            assertThrows(IllegalStateException.class, httpClient::samplePair);
            assertThrows(IllegalStateException.class, httpClient::sampleTriple);
        }
    }

    @SuppressWarnings({"UnusedReturnValue", "rawtypes"})
    @OhClient
    @Mapping("${root}:41196")
    public interface ErrorHttpClient {

        Future sampleFuture();

        List sampleList();

        Map sampleMapError();

        Map sampleMap();

        Pair samplePair();

        Triple sampleTriple();
    }
}
