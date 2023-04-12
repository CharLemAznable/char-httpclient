package com.github.charlemaznable.httpclient.ws.soap;

import com.github.charlemaznable.httpclient.common.VertxReflectFactory;
import com.github.charlemaznable.httpclient.vxclient.VxFactory;
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
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class MockWeatherVxClientTest extends MockWeatherClientTest {

    @Test
    public void testMockWeatherVxClient(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));

        val client = vxLoader.getClient(MockWeatherVxClient.class);

        CompositeFuture.all(newArrayList(
                client.getSupportProvince(new GetSupportProvince.Request())
                        .onSuccess(response -> test.verify(() -> assertEquals(newArrayList("江苏"), response.getResult()))),
                client.getSupportCity(new GetSupportCity.Request())
                        .onSuccess(response -> test.verify(() -> assertEquals(newArrayList("北京", "南京"), response.getResult()))),
                client.getSupportCity(new GetSupportCity.Request().setProvinceName("江苏"))
                        .onSuccess(response -> test.verify(() -> assertEquals(newArrayList("南京"), response.getResult()))))
        ).onComplete(result -> {
            shutdownMockWebServer();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }
}
