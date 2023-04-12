package com.github.charlemaznable.httpclient.ws.soap;

import com.github.charlemaznable.httpclient.ohclient.OhFactory;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportCity;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportProvince;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MockWeatherOhClientTest extends MockWeatherClientTest {

    @Test
    public void testMockWeatherOhClient() {
        startMockWebServer();

        val ohLoader = OhFactory.ohLoader(reflectFactory());

        val client = ohLoader.getClient(MockWeatherOhClient.class);

        val response = client.getSupportProvince(new GetSupportProvince.Request());
        assertEquals(newArrayList("江苏"), response.getResult());

        val response1 = client.getSupportCity(new GetSupportCity.Request());
        assertEquals(newArrayList("北京", "南京"), response1.getResult());

        val response2 = client.getSupportCity(new GetSupportCity.Request().setProvinceName("江苏"));
        assertEquals(newArrayList("南京"), response2.getResult());

        shutdownMockWebServer();
    }
}
