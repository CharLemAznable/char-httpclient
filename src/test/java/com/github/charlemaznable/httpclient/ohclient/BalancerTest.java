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
}
