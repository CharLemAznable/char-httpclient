package com.github.charlemaznable.httpclient.ws.soap;

import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportCity;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportProvince;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.netty.channel.DefaultEventLoop;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.httpclient.vxclient.VxFactory.vxLoader;

@ExtendWith(VertxExtension.class)
public class WeatherVxClientTest {

    @Test
    public void testWeatherVxClient(Vertx vertx, VertxTestContext test) {
        val client = vxLoader(new VertxReflectFactory(vertx)).getClient(WeatherVxClient.class);

        val timeLimiter = TimeLimiter.of(Duration.ofSeconds(15));
        val executor = new DefaultEventLoop();

        Future.all(newArrayList(
                Future.fromCompletionStage(Decorators.ofCompletionStage(() ->
                                client.getSupportProvince(new GetSupportProvince.Request()))
                        .withTimeLimiter(timeLimiter, executor)
                        .withFallback(t -> null).get()),
                Future.fromCompletionStage(Decorators.ofCompletionStage(() ->
                                client.getSupportCity(new GetSupportCity.Request()))
                        .withTimeLimiter(timeLimiter, executor)
                        .withFallback(t -> null).get()),
                Future.fromCompletionStage(Decorators.ofCompletionStage(() ->
                                client.getSupportCity(new GetSupportCity.Request().setProvinceName("江苏")))
                        .withTimeLimiter(timeLimiter, executor)
                        .withFallback(t -> null).get())
        )).onComplete(result -> test.completeNow());
    }
}
