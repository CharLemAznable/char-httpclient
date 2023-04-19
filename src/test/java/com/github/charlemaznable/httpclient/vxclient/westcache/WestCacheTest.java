package com.github.charlemaznable.httpclient.vxclient.westcache;

import com.github.charlemaznable.httpclient.common.westcache.CommonWestCacheTest;
import io.vertx.core.Future;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

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

        // westCacheClient start

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

        // native call twice
        setResult1.set(false);
        setResult2.set(false);
        Future<String> cacheSample1 = westCacheClient.sample();
        cacheSample1.onSuccess(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        Future<String> cacheSample2 = westCacheClient.sample();
        cacheSample2.onSuccess(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        assertNotSame(cacheSample1, cacheSample2);
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

        // rxjava call twice
        setResult1.set(false);
        setResult2.set(false);
        rx.Single<String> cacheSampleRx1 = westCacheClient.sampleRx();
        cacheSampleRx1.subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        rx.Single<String> cacheSampleRx2 = westCacheClient.sampleRx();
        cacheSampleRx2.subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        assertNotSame(cacheSampleRx1, cacheSampleRx2);
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

        // rxjava2 call twice
        setResult1.set(false);
        setResult2.set(false);
        io.reactivex.Single<String> cacheSampleRx21 = westCacheClient.sampleRx2();
        cacheSampleRx21.subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        io.reactivex.Single<String> cacheSampleRx22 = westCacheClient.sampleRx2();
        cacheSampleRx22.subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        assertNotSame(cacheSampleRx21, cacheSampleRx22);
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

        // rxjava3 call twice
        setResult1.set(false);
        setResult2.set(false);
        io.reactivex.rxjava3.core.Single<String> cacheSampleRx31 = westCacheClient.sampleRx3();
        cacheSampleRx31.subscribe(resp -> {
            respResult.setResult1(resp);
            setResult1.set(true);
        });
        io.reactivex.rxjava3.core.Single<String> cacheSampleRx32 = westCacheClient.sampleRx3();
        cacheSampleRx32.subscribe(resp -> {
            respResult.setResult2(resp);
            setResult2.set(true);
        });
        assertNotSame(cacheSampleRx31, cacheSampleRx32);
        await().forever().untilAsserted(() ->
                assertTrue(setResult1.get() && setResult2.get()));
        assertEquals(respResult.getResult1(), respResult.getResult2());

        shutdownMockWebServer();

        assertEquals(36, counter.get());
    }

    @Getter
    @Setter
    private static final class RespResult {
        private String result1;
        private String result2;
    }
}
