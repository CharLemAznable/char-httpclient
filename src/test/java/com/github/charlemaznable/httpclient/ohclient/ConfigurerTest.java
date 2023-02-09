package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.common.ConfigureWith;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.configurer.MappingConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.configservice.OkHttpClientConfig;
import com.github.charlemaznable.httpclient.ohclient.configurer.configservice.OkHttpMethodConfig;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;

import javax.annotation.Nonnull;
import java.util.List;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigurerTest {

    private static final OhFactory.OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @SneakyThrows
    @Test
    public void testConfigurer() {
        MockDiamondServer.setUpMockServer();
        MockDiamondServer.setConfigInfo("ConfigurerClient", "default", "" +
                "baseUrl=${root}:41310\n" +
                "contentFormatter=json\n" +
                "mappingBalancer=random\n" +
                "proxy=http://127.0.0.1:41311\n");
        MockDiamondServer.setConfigInfo("ConfigurerClient", "sample", "" +
                "path=/sample\n" +
                "acceptCharset=UTF88\n" +
                "contentFormatter=jsonn\n" +
                "requestMethod=GETT\n" +
                "fixedContexts=AAA=aaa&BBB\n" +
                "statusFallbackMapping=404=com.github.charlemaznable.httpclient.common.StatusErrorThrower\n" +
                "statusSeriesFallbackMapping=400=com.github.charlemaznable.httpclient.common.StatusErrorThrower\n" +
                "mappingBalancer=randomm\n" +
                "loggingLevel=BASICC\n" +
                "disabledClientProxy=y\n");
        MockDiamondServer.setConfigInfo("ConfigurerClient", "sample2", "" +
                "path=/sample\n" +
                "acceptCharset=UTF88\n" +
                "contentFormatter=jsonn\n" +
                "requestMethod=GETT\n" +
                "fixedContexts=AAA=aaa&BBB\n" +
                "statusFallbackMapping=404=com.github.charlemaznable.httpclient.common.StatusErrorThrower\n" +
                "statusSeriesFallbackMapping=400=com.github.charlemaznable.httpclient.common.StatusErrorThrower\n" +
                "mappingBalancer=randomm\n" +
                "loggingLevel=BASICC\n");

        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    if ("/sample".equals(request.getPath())) {
                        return new MockResponse().setBody("SAMPLE");
                    }
                    return new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                }
            });
            mockWebServer.start(41310);

            val client = ohLoader.getClient(ConfigurerClient.class);
            assertEquals("SAMPLE", client.sample());
            try {
                client.sample2();
            } catch (Exception e) {
                assertEquals("Failed to connect to /127.0.0.1:41311", e.getMessage());
            }

            val clientError = ohLoader.getClient(ConfigurerClientError.class);
            assertEquals("SAMPLE", clientError.sample());
        }

        MockDiamondServer.tearDownMockServer();
    }

    @OhClient
    @ConfigureWith(ConfigurerClientConfig.class)
    public interface ConfigurerClient {

        @ConfigureWith(ConfigurerClientSampleConfig.class)
        String sample();

        @ConfigureWith(ConfigurerClientSample2Config.class)
        void sample2();
    }

    @Config(keyset = "ConfigurerClient", key = "default")
    public interface ConfigurerClientConfig extends OkHttpClientConfig {

        @Override
        @Config("baseUrl")
        String urlsString();
    }

    @Config(keyset = "ConfigurerClient", key = "sample")
    public interface ConfigurerClientSampleConfig extends OkHttpMethodConfig {

        @Override
        @Config("path")
        String urlsString();
    }

    @Config(keyset = "ConfigurerClient", key = "sample2")
    public interface ConfigurerClientSample2Config extends OkHttpMethodConfig {

        @Override
        @Config("path")
        String urlsString();
    }

    @OhClient
    @ConfigureWith(ConfigurerClientErrorConfig.class)
    @Mapping("${root}:41310")
    public interface ConfigurerClientError {

        @ConfigureWith(ConfigurerClientSampleErrorConfig.class)
        String sample();
    }

    public interface ConfigurerClientErrorConfig extends MappingConfigurer {

        @Override
        default List<String> urls() {
            return newArrayList("${root}:41320");
        }
    }

    public interface ConfigurerClientSampleErrorConfig extends MappingConfigurer {

        @Override
        default List<String> urls() {
            return newArrayList("/sample2");
        }
    }
}
