package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingBalance;
import com.github.charlemaznable.httpclient.common.CommonBalancerTest;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BalancerTest extends CommonBalancerTest {

    @Test
    public void testBalancer() {
        startMockWebServer();

        val ohLoader = OhFactory.ohLoader(reflectFactory());

        val httpClient = ohLoader.getClient(BalancerClient.class);

        httpClient.get();
        httpClient.get();
        httpClient.get();
        httpClient.get();
        assertEquals(2, countSample1.get());
        assertEquals(2, countSample2.get());
        assertEquals(0, countSample3.get());

        httpClient.get2();
        httpClient.get2();
        httpClient.get2();
        httpClient.get2();
        assertEquals(4, countSample1.get());
        assertEquals(2, countSample2.get());
        assertEquals(2, countSample3.get());

        httpClient.cover(new MappingBalance.RandomBalancer());

        countSample1.set(0);
        countSample2.set(0);
        countSample3.set(0);

        val httpClientNeo = ohLoader.getClient(BalancerClientNeo.class);

        httpClientNeo.get();
        httpClientNeo.get();
        httpClientNeo.get();
        httpClientNeo.get();
        assertEquals(2, countSample1.get());
        assertEquals(2, countSample2.get());
        assertEquals(0, countSample3.get());

        httpClientNeo.get2();
        httpClientNeo.get2();
        httpClientNeo.get2();
        httpClientNeo.get2();
        assertEquals(4, countSample1.get());
        assertEquals(2, countSample2.get());
        assertEquals(2, countSample3.get());

        httpClientNeo.cover(new MappingBalance.RandomBalancer());

        countSample1.set(0);
        countSample2.set(0);
        countSample3.set(0);

        val httpClientWeighted = ohLoader.getClient(WeightedBalancerClient.class);

        for (int i = 0; i < 30; i++) {
            httpClientWeighted.roundRobin();
        }
        assertEquals(20, countSample1.get());
        assertEquals(10, countSample2.get());
        assertEquals(0, countSample3.get());

        countSample1.set(0);
        countSample2.set(0);
        countSample3.set(0);

        val count = 10000;
        for (int i = 0; i < count; i++) {
            httpClientWeighted.random();
        }
        assertEquals(0, countSample1.get());
        assertEquals(0.25, 1. * countSample2.get() / count, 0.05);
        assertEquals(0.75, 1. * countSample3.get() / count, 0.05);

        shutdownMockWebServer();
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
        void cover(MappingBalance.MappingBalancer balancer);
    }

    @OhClient
    @ConfigureWith(RoundRobinBalancerConfig.class)
    public interface BalancerClientNeo {

        @Mapping({"/sample1", "/sample2"})
        void get();

        @ConfigureWith(MyBalancerConfig.class)
        void get2();

        @ConfigureWith(RandomBalancerConfig.class)
        void cover(MappingBalance.MappingBalancer balancer);
    }

    @OhClient
    @Mapping({"${root}:41240", "${root}:41250"})
    public interface WeightedBalancerClient {

        @Mapping({"/sample1", "/sample1", "/sample2"})
        @MappingBalance(MappingBalance.RoundRobinBalancer.class)
        void roundRobin();

        @Mapping({"/sample2", "/sample3", "/sample3", "/sample3"})
        @MappingBalance(MappingBalance.RandomBalancer.class)
        void random();
    }
}
