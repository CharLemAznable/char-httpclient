package com.github.charlemaznable.httpclient.ws.soap12;

import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportCity;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportProvince;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.httpclient.vxclient.VxFactory.vxLoader;

@ExtendWith(VertxExtension.class)
public class WeatherVxClientTest {

    @Test
    public void testWeatherVxClient(Vertx vertx, VertxTestContext test) {
        val client = vxLoader(new VertxReflectFactory(vertx)).getClient(WeatherVxClient.class);

        Future.all(newArrayList(
                client.getSupportProvince(new GetSupportProvince.Request()),
                client.getSupportCity(new GetSupportCity.Request()),
                client.getSupportCity(new GetSupportCity.Request().setProvinceName("山东")))
        ).onComplete(result -> test.<CompositeFuture>succeedingThenComplete().handle(result));
    }
}
