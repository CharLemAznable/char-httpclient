package com.github.charlemaznable.httpclient.vxclient.westcache;

import com.github.charlemaznable.httpclient.common.westcache.CommonWestCacheTest;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Future;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "ResultOfMethodCallIgnored", "ReactiveStreamsUnusedPublisher"})
@SpringJUnitConfig(WestCacheConfiguration.class)
public class WestCacheTest extends CommonWestCacheTest {

    @Autowired
    private NoWestCacheClient noWestCacheClient;
    @Autowired
    private WestCacheClient westCacheClient;

    @SneakyThrows
    @Test
    public void testWestCache() {
        startMockWebServer();
        val setResult1 = new AtomicBoolean();
        val setResult2 = new AtomicBoolean();
        val respResult = new RespResult();

        // noWestCacheClient start

        // native call add counter
        noWestCacheClient.sample();

        // native call once
        setResult1.set(false);
        setResult2.set(false);
        Future<String> noWestCacheSample = noWestCacheClient.sample();
        noWestCacheSample.onSuccess(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        noWestCacheSample.onSuccess(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // native call twice
        setResult1.set(false);
        setResult2.set(false);
        noWestCacheClient.sample().onSuccess(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        noWestCacheClient.sample().onSuccess(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertNotEquals(respResult.getResult1(), respResult.getResult2());

        // java call add counter
        noWestCacheClient.sampleJava();

        // java call once
        java.util.concurrent.Future<String> noWestCacheSampleJava = noWestCacheClient.sampleJava();
        respResult.setResult1(noWestCacheSampleJava.get());
        respResult.setResult2(noWestCacheSampleJava.get());
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // java call twice
        respResult.setResult1(noWestCacheClient.sampleJava().get());
        respResult.setResult2(noWestCacheClient.sampleJava().get());
        assertNotEquals(respResult.getResult1(), respResult.getResult2());

        // reactor call add counter
        noWestCacheClient.sampleMono();

        // reactor call once
        setResult1.set(false);
        setResult2.set(false);
        Mono<String> noWestCacheSampleMono = noWestCacheClient.sampleMono();
        noWestCacheSampleMono.subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        noWestCacheSampleMono.subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // reactor call twice
        setResult1.set(false);
        setResult2.set(false);
        noWestCacheClient.sampleMono().subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        noWestCacheClient.sampleMono().subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertNotEquals(respResult.getResult1(), respResult.getResult2());

        // rxjava call add counter
        noWestCacheClient.sampleRx();

        // rxjava call once
        setResult1.set(false);
        setResult2.set(false);
        rx.Single<String> noWestCacheSampleRx = noWestCacheClient.sampleRx();
        noWestCacheSampleRx.subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        noWestCacheSampleRx.subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // rxjava call twice
        setResult1.set(false);
        setResult2.set(false);
        noWestCacheClient.sampleRx().subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        noWestCacheClient.sampleRx().subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertNotEquals(respResult.getResult1(), respResult.getResult2());

        // rxjava2 call add counter
        noWestCacheClient.sampleRx2();

        // rxjava2 call once
        setResult1.set(false);
        setResult2.set(false);
        io.reactivex.Single<String> noWestCacheSampleRx2 = noWestCacheClient.sampleRx2();
        noWestCacheSampleRx2.subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        noWestCacheSampleRx2.subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // rxjava2 call twice
        setResult1.set(false);
        setResult2.set(false);
        noWestCacheClient.sampleRx2().subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        noWestCacheClient.sampleRx2().subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertNotEquals(respResult.getResult1(), respResult.getResult2());

        // rxjava3 call add counter
        noWestCacheClient.sampleRx3();

        // rxjava3 call once
        setResult1.set(false);
        setResult2.set(false);
        io.reactivex.rxjava3.core.Single<String> noWestCacheSampleRx3 = noWestCacheClient.sampleRx3();
        noWestCacheSampleRx3.subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        noWestCacheSampleRx3.subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // rxjava3 call twice
        setResult1.set(false);
        setResult2.set(false);
        noWestCacheClient.sampleRx3().subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        noWestCacheClient.sampleRx3().subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertNotEquals(respResult.getResult1(), respResult.getResult2());

        // mutiny call add counter
        noWestCacheClient.sampleUni();

        // mutiny call once
        setResult1.set(false);
        setResult2.set(false);
        Uni<String> noWestCacheSampleUni = noWestCacheClient.sampleUni();
        noWestCacheSampleUni.subscribe().with(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        noWestCacheSampleUni.subscribe().with(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // mutiny call twice
        setResult1.set(false);
        setResult2.set(false);
        noWestCacheClient.sampleUni().subscribe().with(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        noWestCacheClient.sampleUni().subscribe().with(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertNotEquals(respResult.getResult1(), respResult.getResult2());

        assertEquals(28, counter.get());

        // westCacheClient none start

        // native call add counter
        westCacheClient.sampleNone();

        // native call once
        setResult1.set(false);
        setResult2.set(false);
        Future<String> noneWestCacheSample = westCacheClient.sampleNone();
        noneWestCacheSample.onSuccess(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        noneWestCacheSample.onSuccess(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // native call twice
        setResult1.set(false);
        setResult2.set(false);
        westCacheClient.sampleNone().onSuccess(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        westCacheClient.sampleNone().onSuccess(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertNotEquals(respResult.getResult1(), respResult.getResult2());

        // java call add counter
        westCacheClient.sampleNoneJava();

        // java call once
        java.util.concurrent.Future<String> noneWestCacheSampleJava = westCacheClient.sampleNoneJava();
        respResult.setResult1(noneWestCacheSampleJava.get());
        respResult.setResult2(noneWestCacheSampleJava.get());
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // java call twice
        respResult.setResult1(westCacheClient.sampleNoneJava().get());
        respResult.setResult2(westCacheClient.sampleNoneJava().get());
        assertNotEquals(respResult.getResult1(), respResult.getResult2());

        // reactor call add counter
        westCacheClient.sampleNoneMono();

        // reactor call once
        setResult1.set(false);
        setResult2.set(false);
        Mono<String> noneWestCacheSampleMono = westCacheClient.sampleNoneMono();
        noneWestCacheSampleMono.subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        noneWestCacheSampleMono.subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // reactor call twice
        setResult1.set(false);
        setResult2.set(false);
        westCacheClient.sampleNoneMono().subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        westCacheClient.sampleNoneMono().subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertNotEquals(respResult.getResult1(), respResult.getResult2());

        // rxjava call add counter
        westCacheClient.sampleNoneRx();

        // rxjava call once
        setResult1.set(false);
        setResult2.set(false);
        rx.Single<String> noneWestCacheSampleRx = westCacheClient.sampleNoneRx();
        noneWestCacheSampleRx.subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        noneWestCacheSampleRx.subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // rxjava call twice
        setResult1.set(false);
        setResult2.set(false);
        westCacheClient.sampleNoneRx().subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        westCacheClient.sampleNoneRx().subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertNotEquals(respResult.getResult1(), respResult.getResult2());

        // rxjava2 call add counter
        westCacheClient.sampleNoneRx2();

        // rxjava2 call once
        setResult1.set(false);
        setResult2.set(false);
        io.reactivex.Single<String> noneWestCacheSampleRx2 = westCacheClient.sampleNoneRx2();
        noneWestCacheSampleRx2.subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        noneWestCacheSampleRx2.subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // rxjava2 call twice
        setResult1.set(false);
        setResult2.set(false);
        westCacheClient.sampleNoneRx2().subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        westCacheClient.sampleNoneRx2().subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertNotEquals(respResult.getResult1(), respResult.getResult2());

        // rxjava3 call add counter
        westCacheClient.sampleNoneRx3();

        // rxjava3 call once
        setResult1.set(false);
        setResult2.set(false);
        io.reactivex.rxjava3.core.Single<String> noneWestCacheSampleRx3 = westCacheClient.sampleNoneRx3();
        noneWestCacheSampleRx3.subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        noneWestCacheSampleRx3.subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // rxjava3 call twice
        setResult1.set(false);
        setResult2.set(false);
        westCacheClient.sampleNoneRx3().subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        westCacheClient.sampleNoneRx3().subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertNotEquals(respResult.getResult1(), respResult.getResult2());

        // mutiny call add counter
        westCacheClient.sampleNoneUni();

        // mutiny call once
        setResult1.set(false);
        setResult2.set(false);
        Uni<String> noneWestCacheSampleUni = westCacheClient.sampleNoneUni();
        noneWestCacheSampleUni.subscribe().with(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        noneWestCacheSampleUni.subscribe().with(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // mutiny call twice
        setResult1.set(false);
        setResult2.set(false);
        westCacheClient.sampleNoneUni().subscribe().with(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        westCacheClient.sampleNoneUni().subscribe().with(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertNotEquals(respResult.getResult1(), respResult.getResult2());

        assertEquals(56, counter.get());

        // westCacheClient start

        // native call twice
        setResult1.set(false);
        setResult2.set(false);
        Future<String> cacheSample1 = westCacheClient.sample();
        Future<String> cacheSample2 = westCacheClient.sample();
        assertNotSame(cacheSample1, cacheSample2);
        cacheSample1.onSuccess(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        cacheSample2.onSuccess(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // native call once
        setResult1.set(false);
        setResult2.set(false);
        Future<String> westCacheSample = westCacheClient.sample();
        westCacheSample.onSuccess(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        westCacheSample.onSuccess(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // java call twice
        java.util.concurrent.Future<String> cacheSampleJava1 = westCacheClient.sampleJava();
        java.util.concurrent.Future<String> cacheSampleJava2 = westCacheClient.sampleJava();
        assertNotSame(cacheSampleJava1, cacheSampleJava2);
        respResult.setResult1(cacheSampleJava1.get());
        respResult.setResult2(cacheSampleJava2.get());
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // java call once
        java.util.concurrent.Future<String> westCacheSampleJava = westCacheClient.sampleJava();
        respResult.setResult1(westCacheSampleJava.get());
        respResult.setResult2(westCacheSampleJava.get());
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // reactor call twice
        setResult1.set(false);
        setResult2.set(false);
        Mono<String> cacheSampleMono1 = westCacheClient.sampleMono();
        Mono<String> cacheSampleMono2 = westCacheClient.sampleMono();
        assertNotSame(cacheSampleMono1, cacheSampleMono2);
        cacheSampleMono1.subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        cacheSampleMono2.subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // reactor call once
        setResult1.set(false);
        setResult2.set(false);
        Mono<String> westCacheSampleMono = westCacheClient.sampleMono();
        westCacheSampleMono.subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        westCacheSampleMono.subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // rxjava call twice
        setResult1.set(false);
        setResult2.set(false);
        rx.Single<String> cacheSampleRx1 = westCacheClient.sampleRx();
        rx.Single<String> cacheSampleRx2 = westCacheClient.sampleRx();
        assertNotSame(cacheSampleRx1, cacheSampleRx2);
        cacheSampleRx1.subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        cacheSampleRx2.subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // rxjava call once
        setResult1.set(false);
        setResult2.set(false);
        rx.Single<String> westCacheSampleRx = westCacheClient.sampleRx();
        westCacheSampleRx.subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        westCacheSampleRx.subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // rxjava2 call twice
        setResult1.set(false);
        setResult2.set(false);
        io.reactivex.Single<String> cacheSampleRx21 = westCacheClient.sampleRx2();
        io.reactivex.Single<String> cacheSampleRx22 = westCacheClient.sampleRx2();
        assertNotSame(cacheSampleRx21, cacheSampleRx22);
        cacheSampleRx21.subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        cacheSampleRx22.subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // rxjava2 call once
        setResult1.set(false);
        setResult2.set(false);
        io.reactivex.Single<String> westCacheSampleRx2 = westCacheClient.sampleRx2();
        westCacheSampleRx2.subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        westCacheSampleRx2.subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // rxjava3 call twice
        setResult1.set(false);
        setResult2.set(false);
        io.reactivex.rxjava3.core.Single<String> cacheSampleRx31 = westCacheClient.sampleRx3();
        io.reactivex.rxjava3.core.Single<String> cacheSampleRx32 = westCacheClient.sampleRx3();
        assertNotSame(cacheSampleRx31, cacheSampleRx32);
        cacheSampleRx31.subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        cacheSampleRx32.subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // rxjava3 call once
        setResult1.set(false);
        setResult2.set(false);
        io.reactivex.rxjava3.core.Single<String> westCacheSampleRx3 = westCacheClient.sampleRx3();
        westCacheSampleRx3.subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        westCacheSampleRx3.subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // mutiny call twice
        setResult1.set(false);
        setResult2.set(false);
        Uni<String> cacheSampleUni1 = westCacheClient.sampleUni();
        Uni<String> cacheSampleUni2 = westCacheClient.sampleUni();
        assertNotSame(cacheSampleUni1, cacheSampleUni2);
        cacheSampleUni1.subscribe().with(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        cacheSampleUni2.subscribe().with(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        // mutiny call once
        setResult1.set(false);
        setResult2.set(false);
        Uni<String> westCacheSampleUni = westCacheClient.sampleUni();
        westCacheSampleUni.subscribe().with(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        westCacheSampleUni.subscribe().with(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        shutdownMockWebServer();

        assertEquals(63, counter.get());
    }

    @Getter
    @Setter
    private static final class RespResult {
        private String result1;
        private String result2;
    }
}
