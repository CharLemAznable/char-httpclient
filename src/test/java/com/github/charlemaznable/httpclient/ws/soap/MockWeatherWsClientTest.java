package com.github.charlemaznable.httpclient.ws.soap;

import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportCity;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportProvince;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.httpclient.ohclient.OhFactory.getClient;
import static com.github.charlemaznable.httpclient.ws.common.Constants.SOAP_ACTION_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MockWeatherWsClientTest {

    private MockWeatherWsClient client = getClient(MockWeatherWsClient.class);

    @SneakyThrows
    @Test
    public void testMockWeatherWsClient() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    val requestUrl = request.getRequestUrl();
                    assertEquals("/ws", requestUrl.encodedPath());
                    val body = request.getBody().readUtf8();
                    if (GetSupportProvince.SOAP_ACTION.equals(request.getHeader(SOAP_ACTION_KEY))) {
                        val requestEntity = new RequestEntity()
                                .fromXml(body, GetSupportProvince.Request.class);
                        val content = requestEntity.getBody().getContent();
                        assertTrue(content instanceof GetSupportProvince.Request);
                        val provinceResponse = new GetSupportProvince.Response();
                        provinceResponse.setResult(newArrayList("江苏"));
                        return new MockResponse().setBody(new ResponseEntity()
                                .withContent(provinceResponse).toXml());

                    } else if (GetSupportCity.SOAP_ACTION.equals(request.getHeader(SOAP_ACTION_KEY))) {
                        val requestEntity = new RequestEntity()
                                .fromXml(body, GetSupportCity.Request.class);
                        val content = requestEntity.getBody().getContent();
                        assertTrue(content instanceof GetSupportCity.Request);
                        val cityRequest = (GetSupportCity.Request) content;

                        if ("".equals(cityRequest.getProvinceName())) {
                            val cityResponse = new GetSupportCity.Response();
                            cityResponse.setResult(newArrayList("北京", "南京"));
                            return new MockResponse().setBody(new ResponseEntity()
                                    .withContent(cityResponse).toXml());

                        } else if ("江苏".equals(cityRequest.getProvinceName())) {
                            val cityResponse = new GetSupportCity.Response();
                            cityResponse.setResult(newArrayList("南京"));
                            return new MockResponse().setBody(new ResponseEntity()
                                    .withContent(cityResponse).toXml());

                        } else return new MockResponse()
                                .setResponseCode(HttpStatus.NOT_FOUND.value())
                                .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());

                    } else return new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                }
            });
            mockWebServer.start(41240);

            val response = client.getSupportProvince(new GetSupportProvince.Request());
            assertEquals(newArrayList("江苏"), response.getResult());

            val response1 = client.getSupportCity(new GetSupportCity.Request());
            assertEquals(newArrayList("北京", "南京"), response1.getResult());

            val response2 = client.getSupportCity(new GetSupportCity.Request().setProvinceName("江苏"));
            assertEquals(newArrayList("南京"), response2.getResult());
        }
    }
}
