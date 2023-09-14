package com.github.charlemaznable.httpclient.ws.soap12;

import com.github.charlemaznable.httpclient.wfclient.WfFactory;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportCity;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportProvince;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.netty.channel.DefaultEventLoop;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;

public class WeatherWfClientTest {

    @SneakyThrows
    @Test
    public void testWeatherWfClient() {
        val wfLoader = WfFactory.wfLoader(reflectFactory());

        val client = wfLoader.getClient(WeatherWfClient.class);

        val timeLimiter = TimeLimiter.of(Duration.ofSeconds(15));
        val executor = new DefaultEventLoop();

        Decorators.ofCompletionStage(() ->
                        client.getSupportProvince(new GetSupportProvince.Request()))
                .withTimeLimiter(timeLimiter, executor)
                .withFallback(t -> null).get().toCompletableFuture().get();
        Decorators.ofCompletionStage(() ->
                        client.getSupportCity(new GetSupportCity.Request()))
                .withTimeLimiter(timeLimiter, executor)
                .withFallback(t -> null).get().toCompletableFuture().get();
        Decorators.ofCompletionStage(() ->
                        client.getSupportCity(new GetSupportCity.Request().setProvinceName("山东")))
                .withTimeLimiter(timeLimiter, executor)
                .withFallback(t -> null).get().toCompletableFuture().get();
    }
}
