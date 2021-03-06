package com.github.charlemaznable.httpclient.ws.soap;

import com.github.charlemaznable.httpclient.ws.entity.GetSupportCity;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportProvince;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.httpclient.ohclient.OhFactory.getClient;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class WeatherWsClientTest {

    private WeatherWsClient client = getClient(WeatherWsClient.class);

    @Test
    public void testWeatherWsClient() {
        assertDoesNotThrow(() ->
                client.getSupportProvince(new GetSupportProvince.Request()));

        assertDoesNotThrow(() ->
                client.getSupportCity(new GetSupportCity.Request()));

        assertDoesNotThrow(() ->
                client.getSupportCity(new GetSupportCity.Request().setProvinceName("江苏")));
    }
}
