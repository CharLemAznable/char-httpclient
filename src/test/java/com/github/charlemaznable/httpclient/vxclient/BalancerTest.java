package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingBalance;
import com.github.charlemaznable.httpclient.common.CommonBalancerTest;
import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class BalancerTest extends CommonBalancerTest {

    @Test
    public void testBalancer(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));

        val httpClient = vxLoader.getClient(BalancerClient.class);
        val httpClientNeo = vxLoader.getClient(BalancerClientNeo.class);

        Future.all(newArrayList(
                httpClient.get(),
                httpClient.get(),
                httpClient.get(),
                httpClient.get()
        )).compose(result -> {
            assertEquals(2, countSample1.get());
            assertEquals(2, countSample2.get());
            assertEquals(0, countSample3.get());

            return Future.all(newArrayList(
                    httpClient.get2(),
                    httpClient.get2(),
                    httpClient.get2(),
                    httpClient.get2()
            ));
        }).compose(result -> {
            assertEquals(4, countSample1.get());
            assertEquals(2, countSample2.get());
            assertEquals(2, countSample3.get());

            return httpClient.cover(new MappingBalance.RandomBalancer());
        }).compose(result -> {
            countSample1.set(0);
            countSample2.set(0);
            countSample3.set(0);

            return Future.all(newArrayList(
                    httpClientNeo.get(),
                    httpClientNeo.get(),
                    httpClientNeo.get(),
                    httpClientNeo.get()
            ));
        }).compose(result -> {
            assertEquals(2, countSample1.get());
            assertEquals(2, countSample2.get());
            assertEquals(0, countSample3.get());

            return Future.all(newArrayList(
                    httpClientNeo.get2(),
                    httpClientNeo.get2(),
                    httpClientNeo.get2(),
                    httpClientNeo.get2()
            ));
        }).compose(result -> {
            assertEquals(4, countSample1.get());
            assertEquals(2, countSample2.get());
            assertEquals(2, countSample3.get());

            return httpClientNeo.cover(new MappingBalance.RandomBalancer());
        }).onComplete(result -> {
            shutdownMockWebServer();
            test.<Void>succeedingThenComplete().handle(result);
        });
    }

    @Mapping({"${root}:41240", "${root}:41250"})
    @VxClient
    @MappingBalance(MappingBalance.RoundRobinBalancer.class)
    public interface BalancerClient {

        @Mapping({"/sample1", "/sample2"})
        Future<Void> get();

        @MappingBalance(MyBalancer.class)
        @Mapping({"/sample1", "/sample2"})
        Future<Void> get2();

        @MappingBalance(MappingBalance.RandomBalancer.class)
        @Mapping({"/sample1", "/sample2", "/sample3"})
        Future<Void> cover(MappingBalance.MappingBalancer balancer);
    }

    @VxClient
    @ConfigureWith(RoundRobinBalancerConfig.class)
    public interface BalancerClientNeo {

        @Mapping({"/sample1", "/sample2"})
        Future<Void> get();

        @ConfigureWith(MyBalancerConfig.class)
        Future<Void> get2();

        @ConfigureWith(RandomBalancerConfig.class)
        Future<Void> cover(MappingBalance.MappingBalancer balancer);
    }
}
