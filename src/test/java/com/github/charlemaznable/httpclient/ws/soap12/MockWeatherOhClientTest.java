package com.github.charlemaznable.httpclient.ws.soap12;

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
        assertEquals(newArrayList("山东"), response.getResult());

        val response1 = client.getSupportCity(new GetSupportCity.Request());
        assertEquals(newArrayList("北京", "济南"), response1.getResult());

        val response2 = client.getSupportCity(new GetSupportCity.Request().setProvinceName("山东"));
        assertEquals(newArrayList("济南"), response2.getResult());

        shutdownMockWebServer();
    }
}
