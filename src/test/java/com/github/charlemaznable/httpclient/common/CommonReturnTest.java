package com.github.charlemaznable.httpclient.common;

import com.github.charlemaznable.httpclient.annotation.Mapping;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import javax.annotation.Nonnull;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.of;
import static java.util.Objects.requireNonNull;

public abstract class CommonReturnTest {

    protected MockWebServer mockWebServer1;
    protected MockWebServer mockWebServer2;

    @SneakyThrows
    protected void startMockWebServer1() {
        mockWebServer1 = new MockWebServer();
        mockWebServer1.setDispatcher(new Dispatcher() {
            @Nonnull
            @Override
            public MockResponse dispatch(@Nonnull RecordedRequest request) {
                return switch (requireNonNull(request.getPath())) {
                    case "/sampleVoid", "/sampleFutureVoid" -> new MockResponse()
                            .setResponseCode(HttpStatus.OK.value());
                    case "/sampleStatusCode" -> new MockResponse()
                            .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .setBody(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
                    case "/sampleFutureStatusCode" -> new MockResponse()
                            .setResponseCode(HttpStatus.NOT_IMPLEMENTED.value())
                            .setBody(HttpStatus.NOT_IMPLEMENTED.getReasonPhrase());
                    default -> new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                };
            }
        });
        mockWebServer1.start(41190);
    }

    @SneakyThrows
    protected void shutdownMockWebServer1() {
        mockWebServer1.shutdown();
    }

    @SneakyThrows
    protected void startMockWebServer2() {
        mockWebServer2 = new MockWebServer();
        mockWebServer2.setDispatcher(new Dispatcher() {
            @Nonnull
            @Override
            public MockResponse dispatch(@Nonnull RecordedRequest request) {
                if ("/sample".equals(request.getPath())) {
                    return new MockResponse()
                            .setResponseCode(HttpStatus.OK.value())
                            .setBody(HttpStatus.OK.getReasonPhrase());
                } else if ("/sampleObject".equals(request.getPath())) {
                        return new MockResponse()
                                .setResponseCode(HttpStatus.OK.value())
                                .setBody(json(of("John", "Doe")));
                } else if ("/sampleArray".equals(request.getPath())) {
                    return new MockResponse()
                            .setResponseCode(HttpStatus.OK.value())
                            .setBody(json(newArrayList("John", "Doe")));
                } else {
                    return new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                }
            }
        });
        mockWebServer2.start(41191);
    }

    @SneakyThrows
    protected void shutdownMockWebServer2() {
        mockWebServer2.shutdown();
    }

    @Documented
    @Inherited
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Mapping("${root}:41191/sample")
    public @interface TestMapping {}
}
