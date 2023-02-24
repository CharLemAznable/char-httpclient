package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.configservice.ConfigListener;
import com.github.charlemaznable.configservice.ConfigListenerRegister;
import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.httpclient.common.ConfigureWith;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.configurer.MappingConfigurer;
import com.github.charlemaznable.httpclient.ohclient.OhFactory.OhLoader;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.util.List;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Condition.notNullThenRun;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReloaderTest {

    private static final OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @SneakyThrows
    @Test
    public void testReloader() {
        try (val mockWebServer1 = new MockWebServer();
             val mockWebServer2 = new MockWebServer()) {
            mockWebServer1.setDispatcher(new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    val requestUrl = requireNonNull(request.getRequestUrl());
                    if ("/sample".equals(requestUrl.encodedPath())) {
                        return new MockResponse().setBody("mock server 1");
                    }
                    return new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                }
            });
            mockWebServer1.start(41270);
            mockWebServer2.setDispatcher(new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    val requestUrl = requireNonNull(request.getRequestUrl());
                    if ("/sample".equals(requestUrl.encodedPath())) {
                        return new MockResponse().setBody("mock server 2");
                    } else if ("/sample2".equals(requestUrl.encodedPath())) {
                        return new MockResponse().setBody("mock server 3");
                    }
                    return new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                }
            });
            mockWebServer2.start(41280);

            val httpClient = ohLoader.getClient(ReloadableClient.class);
            val response1 = httpClient.sample();
            assertEquals("mock server 1", response1);

            UrlReloader.setBaseUrl("${root}:41280");
            val response2 = httpClient.sample();
            assertEquals("mock server 2", response2);

            SampleReloader.setSamplePath("/sample2");
            val response3 = httpClient.sample();
            assertEquals("mock server 3", response3);

            httpClient.reload();
            val response4 = httpClient.sample();
            assertEquals("mock server 3", response4);
        }
    }

    @OhClient
    @ConfigureWith(UrlReloader.class)
    public interface ReloadableClient extends Reloadable {

        @ConfigureWith(SampleReloader.class)
        String sample();
    }

    public static class UrlReloader implements MappingConfigurer, ConfigListenerRegister {

        private static String baseUrl = "${root}:41270";
        private static ConfigListener listener;

        public static void setBaseUrl(String baseUrl) {
            UrlReloader.baseUrl = baseUrl;
            notNullThenRun(listener, l -> l.onChange(null, null, null));
        }

        @Override
        public void addConfigListener(ConfigListener listener) {
            UrlReloader.listener = listener;
        }

        @Override
        public void removeConfigListener(ConfigListener listener) {
            UrlReloader.listener = null;
        }

        @Override
        public void addConfigListener(String key, ConfigListener listener) {
            UrlReloader.listener = listener;
        }

        @Override
        public void removeConfigListener(String key, ConfigListener listener) {
            UrlReloader.listener = null;
        }

        @Override
        public List<String> urls() {
            return newArrayList(baseUrl);
        }
    }

    public static class SampleReloader implements MappingConfigurer, ConfigListenerRegister {

        private static String samplePath = "/sample";
        private static ConfigListener listener;

        public static void setSamplePath(String samplePath) {
            SampleReloader.samplePath = samplePath;
            notNullThenRun(listener, l -> l.onChange(null, null, null));
        }

        @Override
        public void addConfigListener(ConfigListener listener) {
            SampleReloader.listener = listener;
        }

        @Override
        public void removeConfigListener(ConfigListener listener) {
            SampleReloader.listener = null;
        }

        @Override
        public void addConfigListener(String key, ConfigListener listener) {
            SampleReloader.listener = listener;
        }

        @Override
        public void removeConfigListener(String key, ConfigListener listener) {
            SampleReloader.listener = null;
        }

        @Override
        public List<String> urls() {
            return newArrayList(samplePath);
        }
    }
}
