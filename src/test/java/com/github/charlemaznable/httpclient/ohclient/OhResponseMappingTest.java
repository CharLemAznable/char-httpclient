package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.core.lang.Mapp;
import com.github.charlemaznable.httpclient.common.ConfigureWith;
import com.github.charlemaznable.httpclient.common.DefaultFallbackDisabled;
import com.github.charlemaznable.httpclient.common.FallbackFunction;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.common.StatusError;
import com.github.charlemaznable.httpclient.common.StatusFallback;
import com.github.charlemaznable.httpclient.common.StatusSeriesFallback;
import com.github.charlemaznable.httpclient.configurer.DefaultFallbackDisabledConfigurer;
import com.github.charlemaznable.httpclient.configurer.StatusFallbacksConfigurer;
import com.github.charlemaznable.httpclient.configurer.StatusSeriesFallbacksConfigurer;
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

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("rawtypes")
public class OhResponseMappingTest {

    private static final OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @SneakyThrows
    @Test
    public void testOhResponseMapping() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    switch (requireNonNull(request.getPath())) {
                        case "/sampleNotFound":
                        case "/sampleMappingNotFound":
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                        case "/sampleClientError":
                        case "/sampleMappingClientError":
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.FORBIDDEN.value())
                                    .setBody(HttpStatus.FORBIDDEN.getReasonPhrase());
                        case "/sampleServerError":
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                    .setBody(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
                        default:
                            return new MockResponse().setBody("OK");
                    }
                }
            });
            mockWebServer.start(41180);

            val httpClient = ohLoader.getClient(MappingHttpClient.class);
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), httpClient.sampleNotFound());
            assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), httpClient.sampleClientError());
            assertEquals("\"" + HttpStatus.NOT_FOUND.getReasonPhrase() + "\"", httpClient.sampleMappingNotFound());
            assertEquals("\"" + HttpStatus.FORBIDDEN.getReasonPhrase() + "\"", httpClient.sampleMappingClientError());
            assertThrows(StatusError.class, httpClient::sampleServerError);

            val defaultHttpClient = ohLoader.getClient(DefaultMappingHttpClient.class);
            try {
                defaultHttpClient.sampleNotFound();
            } catch (Exception e) {
                assertEquals(StatusError.class, e.getClass());
                StatusError er = (StatusError) e;
                assertEquals(HttpStatus.NOT_FOUND.value(), er.getStatusCode());
                assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), er.getMessage());
            }
            try {
                defaultHttpClient.sampleClientError();
            } catch (Exception e) {
                assertEquals(StatusError.class, e.getClass());
                StatusError er = (StatusError) e;
                assertEquals(HttpStatus.FORBIDDEN.value(), er.getStatusCode());
                assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), er.getMessage());
            }
            try {
                defaultHttpClient.sampleMappingNotFound();
            } catch (Exception e) {
                assertEquals(StatusError.class, e.getClass());
                StatusError er = (StatusError) e;
                assertEquals(HttpStatus.NOT_FOUND.value(), er.getStatusCode());
                assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), er.getMessage());
            }
            try {
                defaultHttpClient.sampleMappingClientError();
            } catch (Exception e) {
                assertEquals(StatusError.class, e.getClass());
                StatusError er = (StatusError) e;
                assertEquals(HttpStatus.FORBIDDEN.value(), er.getStatusCode());
                assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), er.getMessage());
            }
            try {
                defaultHttpClient.sampleServerError();
            } catch (Exception e) {
                assertEquals(StatusError.class, e.getClass());
                StatusError er = (StatusError) e;
                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), er.getStatusCode());
                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), er.getMessage());
            }

            val disabledHttpClient = ohLoader.getClient(DisabledMappingHttpClient.class);
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), disabledHttpClient.sampleNotFound());
            assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), disabledHttpClient.sampleClientError());
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), disabledHttpClient.sampleMappingNotFound());
            assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), disabledHttpClient.sampleMappingClientError());
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), disabledHttpClient.sampleServerError());

            val httpClientNeo = ohLoader.getClient(MappingHttpClientNeo.class);
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), httpClientNeo.sampleNotFound());
            assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), httpClientNeo.sampleClientError());
            assertEquals("\"" + HttpStatus.NOT_FOUND.getReasonPhrase() + "\"", httpClientNeo.sampleMappingNotFound());
            assertEquals("\"" + HttpStatus.FORBIDDEN.getReasonPhrase() + "\"", httpClientNeo.sampleMappingClientError());
            assertThrows(StatusError.class, httpClientNeo::sampleServerError);

            val disabledHttpClientNeo = ohLoader.getClient(DisabledMappingHttpClientNeo.class);
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), disabledHttpClientNeo.sampleNotFound());
            assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), disabledHttpClientNeo.sampleClientError());
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), disabledHttpClientNeo.sampleMappingNotFound());
            assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), disabledHttpClientNeo.sampleMappingClientError());
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), disabledHttpClientNeo.sampleServerError());
        }
    }

    @StatusFallback(status = HttpStatus.NOT_FOUND, fallback = NotFound.class)
    @StatusSeriesFallback(statusSeries = HttpStatus.Series.CLIENT_ERROR, fallback = ClientError.class)
    @Mapping("${root}:41180")
    @OhClient
    public interface MappingHttpClient {

        String sampleNotFound();

        String sampleClientError();

        @StatusFallback(status = HttpStatus.NOT_FOUND, fallback = NotFound2.class)
        @StatusSeriesFallback(statusSeries = HttpStatus.Series.CLIENT_ERROR, fallback = ClientError2.class)
        String sampleMappingNotFound();

        @StatusFallback(status = HttpStatus.NOT_FOUND, fallback = NotFound2.class)
        @StatusSeriesFallback(statusSeries = HttpStatus.Series.CLIENT_ERROR, fallback = ClientError2.class)
        String sampleMappingClientError();

        @SuppressWarnings("UnusedReturnValue")
        String sampleServerError();
    }

    @Mapping("${root}:41180")
    @OhClient
    public interface DefaultMappingHttpClient {

        void sampleNotFound();

        void sampleClientError();

        void sampleMappingNotFound();

        void sampleMappingClientError();

        void sampleServerError();
    }

    @DefaultFallbackDisabled
    @Mapping("${root}:41180")
    @OhClient
    public interface DisabledMappingHttpClient {

        String sampleNotFound();

        String sampleClientError();

        String sampleMappingNotFound();

        String sampleMappingClientError();

        String sampleServerError();
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

    @Mapping("${root}:41180")
    @OhClient
    @ConfigureWith(MappingHttpClientConfig.class)
    public interface MappingHttpClientNeo {

        String sampleNotFound();

        String sampleClientError();

        @ConfigureWith(SampleMappingConfig.class)
        String sampleMappingNotFound();

        @ConfigureWith(SampleMappingConfig.class)
        String sampleMappingClientError();

        @SuppressWarnings("UnusedReturnValue")
        String sampleServerError();
    }

    public static class MappingHttpClientConfig implements StatusFallbacksConfigurer, StatusSeriesFallbacksConfigurer {

        @Override
        public Map<HttpStatus, Class<? extends FallbackFunction>> statusFallbackMapping() {
            return Mapp.of(HttpStatus.NOT_FOUND, NotFound.class);
        }

        @Override
        public Map<HttpStatus.Series, Class<? extends FallbackFunction>> statusSeriesFallbackMapping() {
            return Mapp.of(HttpStatus.Series.CLIENT_ERROR, ClientError.class);
        }
    }

    public static class SampleMappingConfig implements StatusFallbacksConfigurer, StatusSeriesFallbacksConfigurer {

        @Override
        public Map<HttpStatus, Class<? extends FallbackFunction>> statusFallbackMapping() {
            return Mapp.of(HttpStatus.NOT_FOUND, NotFound2.class);
        }

        @Override
        public Map<HttpStatus.Series, Class<? extends FallbackFunction>> statusSeriesFallbackMapping() {
            return Mapp.of(HttpStatus.Series.CLIENT_ERROR, ClientError2.class);
        }
    }

    @Mapping("${root}:41180")
    @OhClient
    @ConfigureWith(DisabledMappingHttpClientConfig.class)
    public interface DisabledMappingHttpClientNeo {

        String sampleNotFound();

        String sampleClientError();

        String sampleMappingNotFound();

        String sampleMappingClientError();

        String sampleServerError();
    }

    public static class DisabledMappingHttpClientConfig implements DefaultFallbackDisabledConfigurer {}
}
