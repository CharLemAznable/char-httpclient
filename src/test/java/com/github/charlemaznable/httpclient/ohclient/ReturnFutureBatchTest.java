package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.core.lang.Await;
import com.github.charlemaznable.core.lang.Rand;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.Mapping;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newCachedThreadPool;

@Slf4j
public class ReturnFutureBatchTest {

    private static final OhFactory.OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());
    private static final ExecutorService executor = newCachedThreadPool();
    private static final int TIMES = 100;

    @SneakyThrows
    @Test
    public void testBatch() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    val requestUrl = requireNonNull(request.getRequestUrl());
                    switch (requestUrl.encodedPath()) {
                        case "/service1":
                            Await.awaitForMillis(Rand.randInt(100) + 50);
                            return new MockResponse().setBody(toStr(System.currentTimeMillis()));
                        case "/service2":
                            Await.awaitForMillis(Rand.randInt(100) + 150);
                            return new MockResponse().setBody(toStr(System.currentTimeMillis()));
                        case "/service3":
                            Await.awaitForMillis(Rand.randInt(100) + 250);
                            return new MockResponse().setBody(toStr(System.currentTimeMillis()));
                        default: return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            });
            mockWebServer.start(41400);

            val syncClient = ohLoader.getClient(SyncClient.class);
            val asyncClient = ohLoader.getClient(AsyncClient.class);

            val syncStartTime = currentTimeMillis();
            syncBatchRun(syncClient, TIMES);
            val syncBatchRunTime = currentTimeMillis() - syncStartTime;

            log.info("syncBatchRunTime {}", syncBatchRunTime);

            val asyncStartTime = currentTimeMillis();
            asyncBatchRun(asyncClient, TIMES);
            val asyncBatchRunTime = currentTimeMillis() - asyncStartTime;

            log.info("asyncBatchRunTime {}", asyncBatchRunTime);
        }
    }

    public void syncBatchRun(SyncClient syncClient, int times) {
        for (int i = 0; i < times; ++i) {
            val result1 = syncClient.service1();
            log.debug("sync result1 {}", result1);
            val result2 = syncClient.service2();
            log.debug("sync result2 {}", result2);
            val result3 = syncClient.service3();
            log.debug("sync result3 {}", result3);
        }
    }

    public void asyncBatchRun(AsyncClient asyncClient, int times) {
        for (int i = 0; i < times; ++i) {
            val future1 = CompletableFuture.runAsync(() -> log.debug("async result1 {}", futureGet(asyncClient.service1())), executor);
            val future2 = CompletableFuture.runAsync(() -> log.debug("async result2 {}", futureGet(asyncClient.service2())), executor);
            val future3 = CompletableFuture.runAsync(() -> log.debug("async result3 {}", futureGet(asyncClient.service3())), executor);
            futureGet(CompletableFuture.allOf(future1, future2, future3));
        }
    }

    @SneakyThrows
    private static <T> T futureGet(Future<T> future) {
        return future.get();
    }

    @OhClient
    @Mapping("${root}:41400")
    public interface SyncClient {

        String service1();

        String service2();

        String service3();
    }

    @OhClient
    @Mapping("${root}:41400")
    public interface AsyncClient {

        Future<String> service1();

        Future<String> service2();

        Future<String> service3();
    }
}
