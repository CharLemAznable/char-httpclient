package com.github.charlemaznable.httpclient.wfclient;

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
import reactor.core.publisher.Mono;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FallbackTest extends CommonFallbackTest {

    @Test
    public void testFallback() {
        startMockWebServer();

        val wfLoader = WfFactory.wfLoader(reflectFactory());

        val httpClient = wfLoader.getClient(MappingHttpClient.class);
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), httpClient.sampleNotFound().block());
        assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), httpClient.sampleClientError().block());
        assertEquals("\"" + HttpStatus.NOT_FOUND.getReasonPhrase() + "\"", httpClient.sampleMappingNotFound().block());
        assertEquals("\"" + HttpStatus.FORBIDDEN.getReasonPhrase() + "\"", httpClient.sampleMappingClientError().block());
        assertThrows(StatusError.class, () -> httpClient.sampleServerError().block());

        val defaultHttpClient = wfLoader.getClient(DefaultMappingHttpClient.class);
        try {
            defaultHttpClient.sampleNotFound().block();
        } catch (Exception e) {
            assertEquals(StatusError.class, e.getClass());
            StatusError er = (StatusError) e;
            assertEquals(HttpStatus.NOT_FOUND.value(), er.getStatusCode());
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), er.getMessage());
        }
        try {
            defaultHttpClient.sampleClientError().block();
        } catch (Exception e) {
            assertEquals(StatusError.class, e.getClass());
            StatusError er = (StatusError) e;
            assertEquals(HttpStatus.FORBIDDEN.value(), er.getStatusCode());
            assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), er.getMessage());
        }
        try {
            defaultHttpClient.sampleMappingNotFound().block();
        } catch (Exception e) {
            assertEquals(StatusError.class, e.getClass());
            StatusError er = (StatusError) e;
            assertEquals(HttpStatus.NOT_FOUND.value(), er.getStatusCode());
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), er.getMessage());
        }
        try {
            defaultHttpClient.sampleMappingClientError().block();
        } catch (Exception e) {
            assertEquals(StatusError.class, e.getClass());
            StatusError er = (StatusError) e;
            assertEquals(HttpStatus.FORBIDDEN.value(), er.getStatusCode());
            assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), er.getMessage());
        }
        try {
            defaultHttpClient.sampleServerError().block();
        } catch (Exception e) {
            assertEquals(StatusError.class, e.getClass());
            StatusError er = (StatusError) e;
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), er.getStatusCode());
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), er.getMessage());
        }

        val disabledHttpClient = wfLoader.getClient(DisabledMappingHttpClient.class);
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), disabledHttpClient.sampleNotFound().block());
        assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), disabledHttpClient.sampleClientError().block());
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), disabledHttpClient.sampleMappingNotFound().block());
        assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), disabledHttpClient.sampleMappingClientError().block());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), disabledHttpClient.sampleServerError().block());

        val httpClientNeo = wfLoader.getClient(MappingHttpClientNeo.class);
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), httpClientNeo.sampleNotFound().block());
        assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), httpClientNeo.sampleClientError().block());
        assertEquals("\"" + HttpStatus.NOT_FOUND.getReasonPhrase() + "\"", httpClientNeo.sampleMappingNotFound().block());
        assertEquals("\"" + HttpStatus.FORBIDDEN.getReasonPhrase() + "\"", httpClientNeo.sampleMappingClientError().block());
        assertThrows(StatusError.class, () -> httpClientNeo.sampleServerError().block());

        val disabledHttpClientNeo = wfLoader.getClient(DisabledMappingHttpClientNeo.class);
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), disabledHttpClientNeo.sampleNotFound().block());
        assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), disabledHttpClientNeo.sampleClientError().block());
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), disabledHttpClientNeo.sampleMappingNotFound().block());
        assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), disabledHttpClientNeo.sampleMappingClientError().block());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), disabledHttpClientNeo.sampleServerError().block());

        shutdownMockWebServer();
    }

    @StatusFallback(status = HttpStatus.NOT_FOUND, fallback = NotFound.class)
    @StatusSeriesFallback(statusSeries = HttpStatus.Series.CLIENT_ERROR, fallback = ClientError.class)
    @Mapping("${root}:41180")
    @WfClient
    public interface MappingHttpClient {

        Mono<String> sampleNotFound();

        Mono<String> sampleClientError();

        @StatusFallback(status = HttpStatus.NOT_FOUND, fallback = NotFound2.class)
        @StatusSeriesFallback(statusSeries = HttpStatus.Series.CLIENT_ERROR, fallback = ClientError2.class)
        Mono<String> sampleMappingNotFound();

        @StatusFallback(status = HttpStatus.NOT_FOUND, fallback = NotFound2.class)
        @StatusSeriesFallback(statusSeries = HttpStatus.Series.CLIENT_ERROR, fallback = ClientError2.class)
        Mono<String> sampleMappingClientError();

        Mono<String> sampleServerError();
    }

    @Mapping("${root}:41180")
    @WfClient
    public interface DefaultMappingHttpClient {

        Mono<Void> sampleNotFound();

        Mono<Void> sampleClientError();

        Mono<Void> sampleMappingNotFound();

        Mono<Void> sampleMappingClientError();

        Mono<Void> sampleServerError();
    }

    @DefaultFallbackDisabled
    @Mapping("${root}:41180")
    @WfClient
    public interface DisabledMappingHttpClient {

        Mono<String> sampleNotFound();

        Mono<String> sampleClientError();

        Mono<String> sampleMappingNotFound();

        Mono<String> sampleMappingClientError();

        Mono<String> sampleServerError();
    }

    @Mapping("${root}:41180")
    @WfClient
    @ConfigureWith(MappingHttpClientConfig.class)
    public interface MappingHttpClientNeo {

        Mono<String> sampleNotFound();

        Mono<String> sampleClientError();

        @ConfigureWith(SampleMappingConfig.class)
        Mono<String> sampleMappingNotFound();

        @ConfigureWith(SampleMappingConfig.class)
        Mono<String> sampleMappingClientError();

        Mono<String> sampleServerError();
    }

    @Mapping("${root}:41180")
    @WfClient
    @ConfigureWith(DisabledMappingHttpClientConfig.class)
    public interface DisabledMappingHttpClientNeo {

        Mono<String> sampleNotFound();

        Mono<String> sampleClientError();

        Mono<String> sampleMappingNotFound();

        Mono<String> sampleMappingClientError();

        Mono<String> sampleServerError();
    }
}
