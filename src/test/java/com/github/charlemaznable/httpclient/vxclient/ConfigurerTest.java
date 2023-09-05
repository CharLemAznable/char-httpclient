package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonConfigurerTest;
import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import io.vertx.core.CompositeFuture;
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
public class ConfigurerTest extends CommonConfigurerTest {

    @Test
    public void testConfigurer(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));

        val client = vxLoader.getClient(ConfigurerClient.class);
        val clientError = vxLoader.getClient(ConfigurerClientError.class);

        Future.all(newArrayList(
                client.sample().onSuccess(response -> test.verify(() -> assertEquals("SAMPLE", response))),
                Future.future(f -> client.sample2().onFailure(ex -> {
                    assertEquals("Connection refused: /127.0.0.1:41311", ex.getMessage());
                    f.complete();
                })),
                clientError.sample().onSuccess(response -> test.verify(() -> assertEquals("SAMPLE", response)))
        )).onComplete(result -> {
            shutdownMockWebServer();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    @VxClient
    @ConfigureWith(ConfigurerClientConfig.class)
    public interface ConfigurerClient {

        @ConfigureWith(ConfigurerClientSampleConfig.class)
        Future<String> sample();

        @ConfigureWith(ConfigurerClientSample2Config.class)
        Future<Void> sample2();
    }

    @VxClient
    @ConfigureWith(ConfigurerClientErrorConfig.class)
    @Mapping("${root}:41310")
    public interface ConfigurerClientError {

        @ConfigureWith(ConfigurerClientSampleErrorConfig.class)
        Future<String> sample();
    }
}
