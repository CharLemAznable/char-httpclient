package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.common.ConfigureWith;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.ohclient.OhFactory.OhLoader;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientTimeout;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientTimeoutConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.IsolatedConnectionPoolConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.IsolatedDispatcherConfigurer;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimeoutTest {

    private static final String SAMPLE = "Sample";
    private static final OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @SneakyThrows
    @Test
    public void testTimeout() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    if ("/sample".equals(request.getPath())) {
                        return new MockResponse().setBody(SAMPLE);
                    }
                    return new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                }
            });
            mockWebServer.start(41210);

            val client1 = ohLoader.getClient(TimeoutHttpClient1.class);
            assertEquals(SAMPLE, client1.sample());

            val client2 = ohLoader.getClient(TimeoutHttpClient2.class);
            assertEquals(SAMPLE, client2.sample());

            val timeout1 = OhFactory.timeout();
            val param1 = ohLoader.getClient(TimeoutParamHttpClient1.class);
            assertEquals(SAMPLE, param1.sample(timeout1));

            val timeout2 = OhFactory.timeout(60_000, 60_000, 60_000, 60_000);
            val param2 = ohLoader.getClient(TimeoutParamHttpClient2.class);
            assertEquals(SAMPLE, param2.sample(timeout2));

            assertEquals(ClientTimeout.class, timeout1.annotationType());
            assertEquals(ClientTimeout.class, timeout2.annotationType());

            val clientNeo = ohLoader.getClient(TimeoutHttpClientNeo.class);
            assertEquals(SAMPLE, clientNeo.sample());
        }
    }

    @OhClient
    @Mapping("${root}:41210")
    @ClientTimeout
    public interface TimeoutHttpClient1 {

        String sample();
    }

    @OhClient
    @Mapping("${root}:41210")
    @ClientTimeout
    public interface TimeoutHttpClient2 {

        @ClientTimeout(
                callTimeout = 60_000,
                connectTimeout = 60_000,
                readTimeout = 60_000,
                writeTimeout = 60_000)
        String sample();
    }

    @OhClient
    @Mapping("${root}:41210")
    @ClientTimeout
    public interface TimeoutParamHttpClient1 {

        String sample(ClientTimeout clientTimeout);
    }

    @OhClient
    @Mapping("${root}:41210")
    @ClientTimeout
    public interface TimeoutParamHttpClient2 {

        @ClientTimeout
        String sample(ClientTimeout clientTimeout);
    }

    @OhClient
    @Mapping("${root}:41210")
    @ConfigureWith(TimeoutHttpClientConfig.class)
    public interface TimeoutHttpClientNeo {

        @ConfigureWith(TimeoutHttpClientConfig.class)
        String sample();
    }

    public static class TimeoutHttpClientConfig implements ClientTimeoutConfigurer,
            IsolatedDispatcherConfigurer, IsolatedConnectionPoolConfigurer {}
}
