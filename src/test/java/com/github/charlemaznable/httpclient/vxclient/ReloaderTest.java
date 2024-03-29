package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.common.CommonReloaderTest;
import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class ReloaderTest extends CommonReloaderTest {

    @Test
    public void testReloader(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        UrlReloader.setBaseUrl("${root}:41270");
        SampleReloader.setSamplePath("/sample");

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));

        val httpClient = vxLoader.getClient(ReloadableClient.class);

        httpClient.sample().compose(response -> {
            assertEquals("mock server 1", response);

            UrlReloader.setBaseUrl("${root}:41280");
            return httpClient.sample();
        }).compose(response -> {
            assertEquals("mock server 2", response);

            SampleReloader.setSamplePath("/sample2");
            return httpClient.sample();
        }).compose(response -> {
            assertEquals("mock server 3", response);

            httpClient.reload();
            return httpClient.sample();
        }).compose(response -> {
            assertEquals("mock server 3", response);

            shutdownMockWebServer();
            return Future.succeededFuture();
        }).onComplete(test.succeedingThenComplete());
    }

    @VxClient
    @ConfigureWith(UrlReloader.class)
    public interface ReloadableClient extends Reloadable {

        @ConfigureWith(SampleReloader.class)
        Future<String> sample();
    }
}
