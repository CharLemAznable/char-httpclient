package com.github.charlemaznable.httpclient.ws.soap12;

import com.github.charlemaznable.httpclient.common.VertxReflectFactory;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportCity;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportProvince;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.httpclient.vxclient.VxFactory.vxLoader;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class MockWeatherVxClientTest extends MockWeatherClientTest {

    @Test
    public void testMockWeatherVxClient(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        val vxLoader = vxLoader(new VertxReflectFactory(vertx));

        val client = vxLoader.getClient(MockWeatherVxClient.class);

        CompositeFuture.all(newArrayList(
                client.getSupportProvince(new GetSupportProvince.Request())
                        .onSuccess(response -> test.verify(() -> assertEquals(newArrayList("山东"), response.getResult()))),
                client.getSupportCity(new GetSupportCity.Request())
                        .onSuccess(response -> test.verify(() -> assertEquals(newArrayList("北京", "济南"), response.getResult()))),
                client.getSupportCity(new GetSupportCity.Request().setProvinceName("山东"))
                        .onSuccess(response -> test.verify(() -> assertEquals(newArrayList("济南"), response.getResult()))))
        ).onComplete(result -> {
            shutdownMockWebServer();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }
}
