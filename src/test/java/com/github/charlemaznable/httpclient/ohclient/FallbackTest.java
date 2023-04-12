package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.DefaultFallbackDisabled;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.StatusFallback;
import com.github.charlemaznable.httpclient.annotation.StatusSeriesFallback;
import com.github.charlemaznable.httpclient.common.CommonFallbackTest;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.StatusError;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FallbackTest extends CommonFallbackTest {

    @Test
    public void testFallback() {
        startMockWebServer();

        val ohLoader = OhFactory.ohLoader(reflectFactory());

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

        shutdownMockWebServer();
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
}
