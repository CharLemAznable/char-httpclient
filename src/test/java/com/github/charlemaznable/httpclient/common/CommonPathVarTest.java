package com.github.charlemaznable.httpclient.common;

import com.github.charlemaznable.httpclient.configurer.FixedPathVarsConfigurer;
import com.github.charlemaznable.httpclient.configurer.MappingMethodNameDisabledConfigurer;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.httpclient.common.Utils.dispatcher;
import static java.util.Objects.requireNonNull;

public abstract class CommonPathVarTest {

    protected MockWebServer mockWebServer;

    @SneakyThrows
    protected void startMockWebServer() {
        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher(request -> switch (requireNonNull(request.getPath())) {
            case "/V1/V2" -> new MockResponse().setBody("V2");
            case "/V1/V3" -> new MockResponse().setBody("V3");
            case "/V1/V4" -> new MockResponse().setBody("V4");
            default -> new MockResponse()
                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
        }));
        mockWebServer.start(41150);
    }

    @SneakyThrows
    protected void shutdownMockWebServer() {
        mockWebServer.shutdown();
    }

    public static class PathVarHttpClientConfig implements MappingMethodNameDisabledConfigurer, FixedPathVarsConfigurer {

        @Override
        public List<Pair<String, String>> fixedPathVars() {
            return newArrayList(Pair.of("P1", "V1"), Pair.of("P2", "V2"));
        }
    }

    public static class SampleMappingConfig implements FixedPathVarsConfigurer {

        @Override
        public List<Pair<String, String>> fixedPathVars() {
            return newArrayList(Pair.of("P2", "V3"));
        }
    }
}
