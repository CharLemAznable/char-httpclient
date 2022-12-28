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

import javax.annotation.Nonnull;
import javax.xml.bind.JAXBContext;
import java.io.StringReader;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.httpclient.ohclient.OhFactory.getClient;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MockWeatherWsClientTest {

    private final MockWeatherWsClient client = getClient(MockWeatherWsClient.class);

    @SneakyThrows
    @Test
    public void testMockWeatherWsClient() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Nonnull
                @SneakyThrows
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    val requestUrl = requireNonNull(request.getRequestUrl());
                    assertEquals("/ws", requestUrl.encodedPath());
                    val body = request.getBody().readUtf8();
                    val context = JAXBContext.newInstance(RequestEntity.class,
                            GetSupportProvince.Request.class, GetSupportCity.Request.class); // used in unmarshal type matching
                    val unmarshaller = context.createUnmarshaller();
                    val requestEntity = (RequestEntity) unmarshaller.unmarshal(new StringReader(body));
                    val content = requestEntity.getBody().getContent();

                    if (content instanceof GetSupportProvince.Request) {
                        val provinceResponse = new GetSupportProvince.Response();
                        provinceResponse.setResult(newArrayList("山东"));
                        return new MockResponse().setBody(new ResponseEntity()
                                .withContent(provinceResponse).toXml());

                    } else if (content instanceof GetSupportCity.Request) {
                        val cityRequest = (GetSupportCity.Request) content;

                        if ("".equals(cityRequest.getProvinceName())) {
                            val cityResponse = new GetSupportCity.Response();
                            cityResponse.setResult(newArrayList("北京", "济南"));
                            return new MockResponse().setBody(new ResponseEntity()
                                    .withContent(cityResponse).toXml());

                        } else if ("山东".equals(cityRequest.getProvinceName())) {
                            val cityResponse = new GetSupportCity.Response();
                            cityResponse.setResult(newArrayList("济南"));
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
            assertEquals(newArrayList("山东"), response.getResult());

            val response1 = client.getSupportCity(new GetSupportCity.Request());
            assertEquals(newArrayList("北京", "济南"), response1.getResult());

            val response2 = client.getSupportCity(new GetSupportCity.Request().setProvinceName("山东"));
            assertEquals(newArrayList("济南"), response2.getResult());
        }
    }
}
