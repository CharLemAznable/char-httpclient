package com.github.charlemaznable.httpclient.common;

import com.github.charlemaznable.configservice.ConfigListener;
import com.github.charlemaznable.configservice.ConfigListenerRegister;
import com.github.charlemaznable.httpclient.configurer.MappingConfigurer;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import java.util.List;

import static com.github.charlemaznable.core.lang.Condition.notNullThenRun;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.httpclient.common.Utils.dispatcher;
import static java.util.Objects.requireNonNull;

public abstract class CommonReloaderTest {

    protected MockWebServer mockWebServer1;
    protected MockWebServer mockWebServer2;

    @SneakyThrows
    protected void startMockWebServer() {
        mockWebServer1 = new MockWebServer();
        mockWebServer1.setDispatcher(dispatcher(request -> {
            val requestUrl = requireNonNull(request.getRequestUrl());
            if ("/sample".equals(requestUrl.encodedPath())) {
                return new MockResponse().setBody("mock server 1");
            }
            return new MockResponse()
                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
        }));
        mockWebServer1.start(41270);

        mockWebServer2 = new MockWebServer();
        mockWebServer2.setDispatcher(dispatcher(request -> {
            val requestUrl = requireNonNull(request.getRequestUrl());
            if ("/sample".equals(requestUrl.encodedPath())) {
                return new MockResponse().setBody("mock server 2");
            } else if ("/sample2".equals(requestUrl.encodedPath())) {
                return new MockResponse().setBody("mock server 3");
            }
            return new MockResponse()
                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
        }));
        mockWebServer2.start(41280);
    }

    @SneakyThrows
    protected void shutdownMockWebServer() {
        mockWebServer1.shutdown();
        mockWebServer2.shutdown();
    }

    public static class UrlReloader implements MappingConfigurer, ConfigListenerRegister {

        private static String baseUrl;
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

        private static String samplePath;
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
