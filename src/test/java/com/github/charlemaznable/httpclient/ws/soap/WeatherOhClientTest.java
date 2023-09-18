package com.github.charlemaznable.httpclient.ws.soap;

import com.github.charlemaznable.httpclient.ohclient.OhFactory;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportCity;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportProvince;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;

public class WeatherOhClientTest {

    @SneakyThrows
    @Test
    public void testWeatherOhClient() {
        val ohLoader = OhFactory.ohLoader(reflectFactory());

        val client = ohLoader.getClient(WeatherOhClient.class);

        client.getSupportProvince(new GetSupportProvince.Request());
        client.getSupportCity(new GetSupportCity.Request());
        client.getSupportCity(new GetSupportCity.Request().setProvinceName("江苏"));
    }
}
