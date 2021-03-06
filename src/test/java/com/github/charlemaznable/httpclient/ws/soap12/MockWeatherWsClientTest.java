package com.github.charlemaznable.httpclient.ws.soap12;

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

import javax.xml.bind.JAXBContext;
import java.io.StringReader;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.httpclient.ohclient.OhFactory.getClient;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MockWeatherWsClientTest {

    private MockWeatherWsClient client = getClient(MockWeatherWsClient.class);

    @SneakyThrows
    @Test
    public void testMockWeatherWsClient() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @SneakyThrows
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    val requestUrl = request.getRequestUrl();
                    assertEquals("/ws", requestUrl.encodedPath());
                    val body = request.getBody().readUtf8();
                    val context = JAXBContext.newInstance(RequestEntity.class,
                            GetSupportProvince.Request.class, GetSupportCity.Request.class); // used in unmarshal type matching
                    val unmarshaller = context.createUnmarshaller();
                    val requestEntity = (RequestEntity) unmarshaller.unmarshal(new StringReader(body));
                    val content = requestEntity.getBody().getContent();

                    if (content instanceof GetSupportProvince.Request) {
                        val provinceResponse = new GetSupportProvince.Response();
                        provinceResponse.setResult(newArrayList("??????"));
                        return new MockResponse().setBody(new ResponseEntity()
                                .withContent(provinceResponse).toXml());

                    } else if (content instanceof GetSupportCity.Request) {
                        val cityRequest = (GetSupportCity.Request) content;

                        if ("".equals(cityRequest.getProvinceName())) {
                            val cityResponse = new GetSupportCity.Response();
                            cityResponse.setResult(newArrayList("??????", "??????"));
                            return new MockResponse().setBody(new ResponseEntity()
                                    .withContent(cityResponse).toXml());

                        } else if ("??????".equals(cityRequest.getProvinceName())) {
                            val cityResponse = new GetSupportCity.Response();
                            cityResponse.setResult(newArrayList("??????"));
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
            mockWebServer.start(41241);

            val response = client.getSupportProvince(new GetSupportProvince.Request());
            assertEquals(newArrayList("??????"), response.getResult());

            val response1 = client.getSupportCity(new GetSupportCity.Request());
            assertEquals(newArrayList("??????", "??????"), response1.getResult());

            val response2 = client.getSupportCity(new GetSupportCity.Request().setProvinceName("??????"));
            assertEquals(newArrayList("??????"), response2.getResult());
        }
    }
}
