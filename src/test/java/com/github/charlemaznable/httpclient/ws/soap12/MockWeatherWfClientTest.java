package com.github.charlemaznable.httpclient.ws.soap12;

import com.github.charlemaznable.httpclient.wfclient.WfFactory;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportCity;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportProvince;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MockWeatherWfClientTest extends MockWeatherClientTest {

    @Test
    public void testMockWeatherWfClient() {
        startMockWebServer();

        val wfLoader = WfFactory.wfLoader(reflectFactory());

        val client = wfLoader.getClient(MockWeatherWfClient.class);

        val response = client.getSupportProvince(new GetSupportProvince.Request()).block();
        assertEquals(newArrayList("山东"), requireNonNull(response).getResult());

        val response1 = client.getSupportCity(new GetSupportCity.Request()).block();
        assertEquals(newArrayList("北京", "济南"), requireNonNull(response1).getResult());

        val response2 = client.getSupportCity(new GetSupportCity.Request().setProvinceName("山东")).block();
        assertEquals(newArrayList("济南"), requireNonNull(response2).getResult());

        shutdownMockWebServer();
    }
}
