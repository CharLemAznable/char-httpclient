package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.common.ConfigureWith;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.common.MappingBalance;
import com.github.charlemaznable.httpclient.configurer.InitializationConfigurer;
import com.github.charlemaznable.httpclient.configurer.InitializationContext;
import com.github.charlemaznable.httpclient.configurer.MappingBalanceConfigurer;
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
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BalancerTest {

    private static final OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());
    private int countSample1 = 0;
    private int countSample2 = 0;
    private int countSample3 = 0;

    @SneakyThrows
    @Test
    public void testBalancer() {
        try (val mockWebServer1 = new MockWebServer();
             val mockWebServer2 = new MockWebServer()) {
            val dispatcher = new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    val requestUrl = requireNonNull(request.getRequestUrl());
                    switch (requestUrl.encodedPath()) {
                        case "/sample1":
                            countSample1++;
                            return new MockResponse().setBody("OK1");
                        case "/sample2":
                            countSample2++;
                            return new MockResponse().setBody("OK2");
                        case "/sample3":
                            countSample3++;
                            return new MockResponse().setBody("OK3");
                        default:
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            };
            mockWebServer1.setDispatcher(dispatcher);
            mockWebServer1.start(41240);
            mockWebServer2.setDispatcher(dispatcher);
            mockWebServer2.start(41250);

            val httpClient = ohLoader.getClient(BalancerClient.class);

            httpClient.get();
            httpClient.get();
            httpClient.get();
            httpClient.get();
            assertEquals(2, countSample1);
            assertEquals(2, countSample2);
            assertEquals(0, countSample3);

            httpClient.get2();
            httpClient.get2();
            httpClient.get2();
            httpClient.get2();
            assertEquals(4, countSample1);
            assertEquals(2, countSample2);
            assertEquals(2, countSample3);

            httpClient.cover();

            countSample1 = 0;
            countSample2 = 0;
            countSample3 = 0;

            val httpClientNeo = ohLoader.getClient(BalancerClientNeo.class);

            httpClientNeo.get();
            httpClientNeo.get();
            httpClientNeo.get();
            httpClientNeo.get();
            assertEquals(2, countSample1);
            assertEquals(2, countSample2);
            assertEquals(0, countSample3);

            httpClientNeo.get2();
            httpClientNeo.get2();
            httpClientNeo.get2();
            httpClientNeo.get2();
            assertEquals(4, countSample1);
            assertEquals(2, countSample2);
            assertEquals(2, countSample3);

            httpClientNeo.cover();
        }
    }

    @Mapping({"${root}:41240", "${root}:41250"})
    @OhClient
    @MappingBalance(MappingBalance.RoundRobinBalancer.class)
    public interface BalancerClient {

        @Mapping({"/sample1", "/sample2"})
        void get();

        @MappingBalance(MyBalancer.class)
        @Mapping({"/sample1", "/sample2"})
        void get2();

        @MappingBalance(MappingBalance.RandomBalancer.class)
        @Mapping({"/sample1", "/sample2", "/sample3"})
        void cover();
    }

    public static class MyBalancer extends MappingBalance.RoundRobinBalancer {

        @Override
        public String choose(List<String> urls) {
            return super.choose(urls).replace("sample2", "sample3");
        }
    }

    @OhClient
    @ConfigureWith(RoundRobinBalancerConfig.class)
    public interface BalancerClientNeo {

        @Mapping({"/sample1", "/sample2"})
        void get();

        @ConfigureWith(MyBalancerConfig.class)
        void get2();

        @ConfigureWith(RandomBalancerConfig.class)
        void cover();
    }

    public static class RoundRobinBalancerConfig implements MappingConfigurer, MappingBalanceConfigurer, InitializationConfigurer {

        @Override
        public List<String> urls() {
            assertEquals(BalancerClientNeo.class, InitializationContext.getOhClass());
            assertNull(InitializationContext.getOhMethod());
            return newArrayList("${root}:41240", "${root}:41250");
        }

        @Override
        public MappingBalance.MappingBalancer mappingBalancer() {
            assertEquals(BalancerClientNeo.class, InitializationContext.getOhClass());
            assertNull(InitializationContext.getOhMethod());
            return new MappingBalance.RoundRobinBalancer();
        }
    }

    public static class MyBalancerConfig implements MappingConfigurer, MappingBalanceConfigurer {

        @Override
        public List<String> urls() {
            assertEquals(BalancerClientNeo.class, InitializationContext.getOhClass());
            assertEquals("get2", InitializationContext.getOhMethod().getName());
            return newArrayList("/sample1", "/sample2");
        }

        @Override
        public MappingBalance.MappingBalancer mappingBalancer() {
            assertEquals(BalancerClientNeo.class, InitializationContext.getOhClass());
            assertEquals("get2", InitializationContext.getOhMethod().getName());
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
