package com.github.charlemaznable.httpclient.wfclient;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingBalance;
import com.github.charlemaznable.httpclient.common.CommonBalancerTest;
import lombok.val;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BalancerTest extends CommonBalancerTest {

    @Test
    public void testBalancer() {
        startMockWebServer();

        val wfLoader = WfFactory.wfLoader(reflectFactory());

        val httpClient = wfLoader.getClient(BalancerClient.class);

        httpClient.get().block();
        httpClient.get().block();
        httpClient.get().block();
        httpClient.get().block();
        assertEquals(2, countSample1.get());
        assertEquals(2, countSample2.get());
        assertEquals(0, countSample3.get());

        httpClient.get2().block();
        httpClient.get2().block();
        httpClient.get2().block();
        httpClient.get2().block();
        assertEquals(4, countSample1.get());
        assertEquals(2, countSample2.get());
        assertEquals(2, countSample3.get());

        httpClient.cover(new MappingBalance.RandomBalancer()).block();

        countSample1.set(0);
        countSample2.set(0);
        countSample3.set(0);

        val httpClientNeo = wfLoader.getClient(BalancerClientNeo.class);

        httpClientNeo.get().block();
        httpClientNeo.get().block();
        httpClientNeo.get().block();
        httpClientNeo.get().block();
        assertEquals(2, countSample1.get());
        assertEquals(2, countSample2.get());
        assertEquals(0, countSample3.get());

        httpClientNeo.get2().block();
        httpClientNeo.get2().block();
        httpClientNeo.get2().block();
        httpClientNeo.get2().block();
        assertEquals(4, countSample1.get());
        assertEquals(2, countSample2.get());
        assertEquals(2, countSample3.get());

        httpClientNeo.cover(new MappingBalance.RandomBalancer()).block();

        shutdownMockWebServer();
    }

    @Mapping({"${root}:41240", "${root}:41250"})
    @WfClient
    @MappingBalance(MappingBalance.RoundRobinBalancer.class)
    public interface BalancerClient {

        @Mapping({"/sample1", "/sample2"})
        Mono<Void> get();

        @MappingBalance(MyBalancer.class)
        @Mapping({"/sample1", "/sample2"})
        Mono<Void> get2();

        @MappingBalance(MappingBalance.RandomBalancer.class)
        @Mapping({"/sample1", "/sample2", "/sample3"})
        Mono<Void> cover(MappingBalance.MappingBalancer balancer);
    }

    @WfClient
    @ConfigureWith(RoundRobinBalancerConfig.class)
    public interface BalancerClientNeo {

        @Mapping({"/sample1", "/sample2"})
        Mono<Void> get();

        @ConfigureWith(MyBalancerConfig.class)
        Mono<Void> get2();

        @ConfigureWith(RandomBalancerConfig.class)
        Mono<Void> cover(MappingBalance.MappingBalancer balancer);
    }
}
