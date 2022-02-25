package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.common.ContentFormat;
import com.github.charlemaznable.httpclient.common.ContentFormat.JsonContentFormatter;
import com.github.charlemaznable.httpclient.common.ExtraUrlQuery;
import com.github.charlemaznable.httpclient.common.ExtraUrlQuery.ExtraUrlQueryBuilder;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.common.Parameter;
import com.github.charlemaznable.httpclient.common.RequestMethod;
import com.github.charlemaznable.httpclient.ohclient.OhFactory.OhLoader;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.util.Map;

import static com.github.charlemaznable.core.codec.Json.unJson;
import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExtraQueryTest {

    private static OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @SneakyThrows
    @Test
    public void testExtraQuery() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    val requestUrl = request.getRequestUrl();
                    switch (requestUrl.encodedPath()) {
                        case "/sampleGet":
                            assertEquals("GET", request.getMethod());
                            assertEquals("EQV1", requestUrl.queryParameter("EQ1"));
                            assertEquals("PV1", requestUrl.queryParameter("P1"));
                            return new MockResponse().setBody("OK");
                        case "/samplePost":
                            assertEquals("POST", request.getMethod());
                            assertEquals("EQV2", requestUrl.queryParameter("EQ1"));
                            val body = unJson(request.getBody().readUtf8());
                            assertEquals("PV1", body.get("P1"));
                            return new MockResponse().setBody("OK");
                        default:
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            });
            mockWebServer.start(41230);

            val httpClient = ohLoader.getClient(ExtraHttpClient.class);
            assertEquals("OK", httpClient.sampleGet("PV1"));
            assertEquals("OK", httpClient.samplePost("PV1"));
        }
    }

    @ExtraUrlQuery(ExtraOnClass.class)
    @ContentFormat(JsonContentFormatter.class)
    @Mapping("${root}:41230")
    @OhClient
    public interface ExtraHttpClient {

        String sampleGet(@Parameter("P1") String p);

        @RequestMethod(HttpMethod.POST)
        @ExtraUrlQuery(ExtraOnMethod.class)
        String samplePost(@Parameter("P1") String p);
    }

    public static class ExtraOnClass implements ExtraUrlQueryBuilder {

        @Override
        public String build(@Nonnull Map<String, Object> parameterMap,
                            @Nonnull Map<String, Object> contextMap) {
            return "EQ1=EQV1";
        }
    }

    public static class ExtraOnMethod implements ExtraUrlQueryBuilder {

        @Override
        public String build(@Nonnull Map<String, Object> parameterMap,
                            @Nonnull Map<String, Object> contextMap) {
            return "EQ1=EQV2";
        }
    }
}
