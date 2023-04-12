package com.github.charlemaznable.httpclient.common;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.configurer.MappingConfigurer;
import com.github.charlemaznable.httpclient.configurer.configservice.CommonClientConfig;
import com.github.charlemaznable.httpclient.configurer.configservice.CommonMethodConfig;
import com.github.charlemaznable.httpclient.ohclient.configurer.OkHttpClientBuilderConfigurer;
import com.github.charlemaznable.httpclient.vxclient.configurer.VertxWebClientOptionsConfigurer;
import io.vertx.core.net.ProxyOptions;
import io.vertx.ext.web.client.WebClientOptions;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.n3r.diamond.client.impl.MockDiamondServer;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.httpclient.configurer.InitializationContext.getInitializingClass;
import static com.github.charlemaznable.httpclient.configurer.InitializationContext.getInitializingMethod;
import static java.net.Proxy.Type.HTTP;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class CommonConfigurerTest {

    protected MockWebServer mockWebServer;

    @SneakyThrows
    protected void startMockWebServer() {
        MockDiamondServer.setUpMockServer();
        MockDiamondServer.setConfigInfo("ConfigurerClient", "default", """
                baseUrl=${root}:41310
                contentFormatter=json
                mappingBalancer=random
                """);
        MockDiamondServer.setConfigInfo("ConfigurerClient", "sample", """
                path=/sample
                acceptCharset=UTF88
                contentFormatter=jsonn
                requestMethod=GETT
                fixedContexts=AAA=aaa&BBB
                statusFallbackMapping=404=com.github.charlemaznable.httpclient.common.StatusErrorFallback
                statusSeriesFallbackMapping=400=com.github.charlemaznable.httpclient.common.StatusErrorFallback
                mappingBalancer=randomm
                """);
        MockDiamondServer.setConfigInfo("ConfigurerClient", "sample2", """
                path=/sample
                acceptCharset=UTF88
                contentFormatter=jsonn
                requestMethod=GETT
                fixedContexts=AAA=aaa&BBB
                statusFallbackMapping=404=com.github.charlemaznable.httpclient.common.StatusErrorFallback
                statusSeriesFallbackMapping=400=com.github.charlemaznable.httpclient.common.StatusErrorFallback
                mappingBalancer=randomm
                """);

        mockWebServer = new MockWebServer();
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
    }

    @SneakyThrows
    protected void shutdownMockWebServer() {
        mockWebServer.shutdown();
        MockDiamondServer.tearDownMockServer();
    }

    @Config(keyset = "ConfigurerClient", key = "default")
    public interface ConfigurerClientConfig extends CommonClientConfig {

        @Override
        @Config("baseUrl")
        String urlsString();
    }

    @Config(keyset = "ConfigurerClient", key = "sample")
    public interface ConfigurerClientSampleConfig extends CommonMethodConfig {

        @Override
        @Config("path")
        String urlsString();
    }

    @Config(keyset = "ConfigurerClient", key = "sample2")
    public interface ConfigurerClientSample2Config extends CommonMethodConfig,
            OkHttpClientBuilderConfigurer, VertxWebClientOptionsConfigurer {

        @Override
        @Config("path")
        String urlsString();

        @Override
        default OkHttpClient.Builder configBuilder(OkHttpClient.Builder builder) {
            assertEquals(com.github.charlemaznable.httpclient.ohclient.ConfigurerTest.ConfigurerClient.class, getInitializingClass());
            assertEquals("sample2", getInitializingMethod().getName());
            return builder.proxy(new Proxy(HTTP, new InetSocketAddress("127.0.0.1", 41311)));
        }

        @Override
        default WebClientOptions configOptions(WebClientOptions options) {
            assertEquals(com.github.charlemaznable.httpclient.vxclient.ConfigurerTest.ConfigurerClient.class, getInitializingClass());
            assertEquals("sample2", getInitializingMethod().getName());
            return options.setProxyOptions(new ProxyOptions().setHost("127.0.0.1").setPort(41311));
        }
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
