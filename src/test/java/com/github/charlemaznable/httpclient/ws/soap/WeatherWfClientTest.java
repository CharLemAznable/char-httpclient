package com.github.charlemaznable.httpclient.ws.soap;

import com.github.charlemaznable.httpclient.wfclient.WfFactory;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportCity;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportProvince;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;

public class WeatherWfClientTest {

    @Test
    public void testWeatherWfClient() {
        val wfLoader = WfFactory.wfLoader(reflectFactory());

        val client = wfLoader.getClient(WeatherWfClient.class);

        client.getSupportProvince(new GetSupportProvince.Request()).block();
        client.getSupportCity(new GetSupportCity.Request()).block();
        client.getSupportCity(new GetSupportCity.Request().setProvinceName("江苏")).block();
    }
}
