package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.core.lang.Await;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.vxclient.VxReq;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static com.github.charlemaznable.core.lang.concurrent.Executors.parallelismExecutor;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.requireNonNull;

@Slf4j
@ExtendWith(VertxExtension.class)
public class ReturnFutureBatchTest {

    private static final OhFactory.OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());
    private static final ExecutorService batcher = parallelismExecutor();
    private static final int TIMES = 100;

    private MockWebServer mockWebServer;

    @Test
    public void testBatch(Vertx vertx, VertxTestContext test) {
        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Nonnull
            @Override
            public MockResponse dispatch(@Nonnull RecordedRequest request) {
                val requestUrl = requireNonNull(request.getRequestUrl());
                switch (requestUrl.encodedPath()) {
                    case "/service1":
                        Await.awaitForMillis(100);
                        return new MockResponse().setBody(toStr(System.currentTimeMillis()));
                    case "/service2":
                        Await.awaitForMillis(200);
                        return new MockResponse().setBody(toStr(System.currentTimeMillis()));
                    case "/service3":
                        Await.awaitForMillis(300);
                        return new MockResponse().setBody(toStr(System.currentTimeMillis()));
                    default:
                        return new MockResponse()
                                .setResponseCode(HttpStatus.NOT_FOUND.value())
                                .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                }
            }
        });
        startMockWebServer();

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

        val vertxStartTime = currentTimeMillis();
        vertxBatchRun(vertx, test, TIMES, () -> {
            val vertxBatchRunTime = currentTimeMillis() - vertxStartTime;
            log.info("vertxBatchRunTime {}", vertxBatchRunTime);
            shutdownMockWebServer();
        });
    }

    @SneakyThrows
    private void startMockWebServer() {
        mockWebServer.start(41400);
    }

    @SneakyThrows
    private void shutdownMockWebServer() {
        mockWebServer.shutdown();
    }

    @SneakyThrows
    public void syncBatchRun(SyncClient syncClient, int times) {
        val service = new Future[times];
        for (int i = 0; i < times; ++i) {
            service[i] = batcher.submit(() -> {
                val result1 = syncClient.service1();
                log.debug("sync result1 {}", result1);
                val result2 = syncClient.service2();
                log.debug("sync result2 {}", result2);
                val result3 = syncClient.service3();
                log.debug("sync result3 {}", result3);
            });
        }
        for (int i = 0; i < times; ++i) {
            service[i].get();
        }
    }

    @SneakyThrows
    public void asyncBatchRun(AsyncClient asyncClient, int times) {
        val service = new Future[times];
        for (int i = 0; i < times; ++i) {
            service[i] = batcher.submit(() -> {
                val future1 = ((CompletableFuture<String>) asyncClient.service1()).thenAccept(s -> log.debug("async result1 {}", s));
                val future2 = ((CompletableFuture<String>) asyncClient.service2()).thenAccept(s -> log.debug("async result2 {}", s));
                val future3 = ((CompletableFuture<String>) asyncClient.service3()).thenAccept(s -> log.debug("async result3 {}", s));
                futureGet(CompletableFuture.allOf(future1, future2, future3));
            });
        }
        for (int i = 0; i < times; ++i) {
            service[i].get();
        }
    }

    @SneakyThrows
    private static <T> void futureGet(Future<T> future) {
        future.get();
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

    public void vertxBatchRun(Vertx vertx, VertxTestContext test, int times, Runnable complete) {
        val service1 = new VxReq(vertx, "http://127.0.0.1:41400/service1").buildGetInstance();
        val service2 = new VxReq(vertx, "http://127.0.0.1:41400/service2").buildGetInstance();
        val service3 = new VxReq(vertx, "http://127.0.0.1:41400/service3").buildGetInstance();
        val futures = new ArrayList<io.vertx.core.Future<CompositeFuture>>();
        for (int i = 0; i < times; ++i) {
            futures.add(io.vertx.core.Future.future(f -> CompositeFuture.all(newArrayList(
                    io.vertx.core.Future.<String>future(f2 -> service1.request(async ->
                            log.debug("vertx result1 {}", async.result()), f2)),
                    io.vertx.core.Future.<String>future(f2 -> service2.request(async ->
                            log.debug("vertx result2 {}", async.result()), f2)),
                    io.vertx.core.Future.<String>future(f2 -> service3.request(async ->
                            log.debug("vertx result3 {}", async.result()), f2))
            )).onComplete(f)));
        }
        CompositeFuture.all(newArrayList(futures)).onComplete(result -> {
            complete.run();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }
}
