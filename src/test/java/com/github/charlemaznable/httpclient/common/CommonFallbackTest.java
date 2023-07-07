package com.github.charlemaznable.httpclient.common;

import com.github.charlemaznable.core.lang.Mapp;
import com.github.charlemaznable.httpclient.configurer.DefaultFallbackDisabledConfigurer;
import com.github.charlemaznable.httpclient.configurer.StatusFallbacksConfigurer;
import com.github.charlemaznable.httpclient.configurer.StatusSeriesFallbacksConfigurer;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import java.util.Map;

import static com.github.charlemaznable.httpclient.common.Utils.dispatcher;
import static java.util.Objects.requireNonNull;

public abstract class CommonFallbackTest {

    protected MockWebServer mockWebServer;

    @SneakyThrows
    protected void startMockWebServer() {
        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher(request -> switch (requireNonNull(request.getPath())) {
            case "/sampleNotFound", "/sampleMappingNotFound" -> new MockResponse()
                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
            case "/sampleClientError", "/sampleMappingClientError" -> new MockResponse()
                    .setResponseCode(HttpStatus.FORBIDDEN.value())
                    .setBody(HttpStatus.FORBIDDEN.getReasonPhrase());
            case "/sampleServerError" -> new MockResponse()
                    .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .setBody(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
            default -> new MockResponse().setBody("OK");
        }));
        mockWebServer.start(41180);
    }

    @SneakyThrows
    protected void shutdownMockWebServer() {
        mockWebServer.shutdown();
    }

    public static class NotFound implements FallbackFunction<String> {

        @Override
        public String apply(Response response) {
            return response.responseBodyAsString();
        }
    }

    public static class ClientError implements FallbackFunction<String> {

        @Override
        public String apply(Response response) {
            return response.responseBodyAsString();
        }
    }

    public static class NotFound2 implements FallbackFunction<String> {

        @Override
        public String apply(Response response) {
            return "\"" + response.responseBodyAsString() + "\"";
        }
    }

    public static class ClientError2 implements FallbackFunction<String> {

        @Override
        public String apply(Response response) {
            return "\"" + response.responseBodyAsString() + "\"";
        }
    }

    public static class MappingHttpClientConfig implements StatusFallbacksConfigurer, StatusSeriesFallbacksConfigurer {

        @Override
        public Map<HttpStatus, FallbackFunction<?>> statusFallbackMapping() {
            return Mapp.of(HttpStatus.NOT_FOUND, new NotFound());
        }

        @Override
        public Map<HttpStatus.Series, FallbackFunction<?>> statusSeriesFallbackMapping() {
            return Mapp.of(HttpStatus.Series.CLIENT_ERROR, new ClientError());
        }
    }

    public static class SampleMappingConfig implements StatusFallbacksConfigurer, StatusSeriesFallbacksConfigurer {

        @Override
        public Map<HttpStatus, FallbackFunction<?>> statusFallbackMapping() {
            return Mapp.of(HttpStatus.NOT_FOUND, new NotFound2());
        }

        @Override
        public Map<HttpStatus.Series, FallbackFunction<?>> statusSeriesFallbackMapping() {
            return Mapp.of(HttpStatus.Series.CLIENT_ERROR, new ClientError2());
        }
    }

    public static class DisabledMappingHttpClientConfig implements DefaultFallbackDisabledConfigurer {
    }
}
