package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonReturnListTest;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class ReturnListTest extends CommonReturnListTest {

    @Test
    public void testList(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));

        val httpClient = vxLoader.getClient(ListHttpClient.class);

        Future.all(newArrayList(
                httpClient.sampleFutureListBean().onSuccess(beans -> test.verify(() -> {
                    val bean1 = beans.get(0);
                    val bean2 = beans.get(1);
                    assertEquals("John", bean1.getName());
                    assertEquals("Doe", bean2.getName());
                })),
                httpClient.sampleFutureListString().onSuccess(strs -> test.verify(() -> {
                    val str1 = strs.get(0);
                    val str2 = strs.get(1);
                    assertEquals("John", str1);
                    assertEquals("Doe", str2);
                })),
                httpClient.sampleFutureListBuffer().onSuccess(buffers -> test.verify(() -> {
                    assertEquals(1, buffers.size());
                    assertEquals(HttpStatus.OK.getReasonPhrase(), buffers.get(0).toString());
                }))
        )).onComplete(result -> {
            shutdownMockWebServer();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    @VxClient
    @Mapping("${root}:41192")
    public interface ListHttpClient {

        Future<List<Bean>> sampleFutureListBean();

        Future<ArrayList<String>> sampleFutureListString();

        Future<List<Buffer>> sampleFutureListBuffer();
    }
}
