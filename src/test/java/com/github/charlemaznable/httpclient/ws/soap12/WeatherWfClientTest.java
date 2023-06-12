package com.github.charlemaznable.httpclient.ws.soap12;

import com.github.charlemaznable.httpclient.wfclient.WfFactory;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportCity;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportProvince;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class WeatherWfClientTest {

    @Test
    public void testWeatherWfClient() {
        val wfLoader = WfFactory.wfLoader(reflectFactory());

        val client = wfLoader.getClient(WeatherWfClient.class);

        assertDoesNotThrow(() ->
                client.getSupportProvince(new GetSupportProvince.Request()).block());

        assertDoesNotThrow(() ->
                client.getSupportCity(new GetSupportCity.Request()).block());

        assertDoesNotThrow(() ->
                client.getSupportCity(new GetSupportCity.Request().setProvinceName("山东")).block());
    }
}
