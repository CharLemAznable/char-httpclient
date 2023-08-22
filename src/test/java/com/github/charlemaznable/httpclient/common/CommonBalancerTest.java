package com.github.charlemaznable.httpclient.common;

import com.github.charlemaznable.httpclient.annotation.MappingBalance;
import com.github.charlemaznable.httpclient.configurer.MappingBalanceConfigurer;
import com.github.charlemaznable.httpclient.configurer.MappingConfigurer;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.httpclient.common.Utils.dispatcher;
import static java.util.Objects.requireNonNull;

public abstract class CommonBalancerTest {

    protected final AtomicInteger countSample1 = new AtomicInteger();
    protected final AtomicInteger countSample2 = new AtomicInteger();
    protected final AtomicInteger countSample3 = new AtomicInteger();

    protected MockWebServer mockWebServer1;
    protected MockWebServer mockWebServer2;

    @SneakyThrows
    protected void startMockWebServer() {
        mockWebServer1 = new MockWebServer();
        mockWebServer2 = new MockWebServer();
        val dispatcher = dispatcher(request -> {
            val requestUrl = requireNonNull(request.getRequestUrl());
            switch (requestUrl.encodedPath()) {
                case "/sample1" -> {
                    countSample1.incrementAndGet();
                    return new MockResponse().setBody("OK1");
                }
                case "/sample2" -> {
                    countSample2.incrementAndGet();
                    return new MockResponse().setBody("OK2");
                }
                case "/sample3" -> {
                    countSample3.incrementAndGet();
                    return new MockResponse().setBody("OK3");
                }
                default -> {
                    return new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                }
            }
        });
        mockWebServer1.setDispatcher(dispatcher);
        mockWebServer1.start(41240);
        mockWebServer2.setDispatcher(dispatcher);
        mockWebServer2.start(41250);
    }

    @SneakyThrows
    protected void shutdownMockWebServer() {
        mockWebServer1.shutdown();
        mockWebServer2.shutdown();
    }

    public static class MyBalancer extends MappingBalance.RoundRobinBalancer {

        @Override
        public String choose(List<String> urls) {
            return super.choose(urls).replace("sample2", "sample3");
        }
    }

    public static class RoundRobinBalancerConfig implements MappingConfigurer, MappingBalanceConfigurer {

        @Override
        public List<String> urls() {
            return newArrayList("${root}:41240", "${root}:41250");
        }

        @Override
        public MappingBalance.MappingBalancer mappingBalancer() {
            return new MappingBalance.RoundRobinBalancer();
        }
    }

    public static class MyBalancerConfig implements MappingConfigurer, MappingBalanceConfigurer {

        @Override
        public List<String> urls() {
            return newArrayList("/sample1", "/sample2");
        }

        @Override
        public MappingBalance.MappingBalancer mappingBalancer() {
            return new MyBalancer();
        }
    }

    public static class RandomBalancerConfig implements MappingConfigurer, MappingBalanceConfigurer {

        @Override
        public List<String> urls() {
            return newArrayList("/sample1", "/sample2", "/sample3");
        }

        @Override
        public MappingBalance.MappingBalancer mappingBalancer() {
            return new MappingBalance.RandomBalancer();
        }
    }
}
