package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.core.config.Arguments;
import com.github.charlemaznable.core.lang.EverythingIsNonNull;
import com.github.charlemaznable.httpclient.common.ConfigureWith;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.common.ProviderException;
import com.github.charlemaznable.httpclient.common.RequestBodyRaw;
import com.github.charlemaznable.httpclient.common.RequestMethod;
import com.github.charlemaznable.httpclient.configurer.RequestMethodConfigurer;
import com.github.charlemaznable.httpclient.ohclient.OhFactory.OhLoader;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientInterceptor;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientInterceptor.InterceptorProvider;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientInterceptorCleanup;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientLoggingLevel;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientLoggingLevel.LoggingLevelProvider;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientInterceptorsCleanupConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientInterceptorsConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientLoggingLevelConfigurer;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InterceptorTest {

    private static final String BODY = "BODY";
    private static final String CONTENT = "OK";
    private static final String HEADER_NAME = "intercept";
    private static final OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @BeforeAll
    public static void beforeAll() {
        Arguments.initial("--port=41220");
    }

    @AfterAll
    public static void afterAll() {
        Arguments.initial();
    }

    @EverythingIsNonNull
    @SneakyThrows
    @Test
    public void testInterceptorClient() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    switch (requireNonNull(request.getPath())) {
                        case "/sample1":
                            val values1 = request.getHeaders().values(HEADER_NAME);
                            assertEquals(1, values1.size());
                            assertEquals("class", values1.get(0));
                            return new MockResponse().setBody(CONTENT);
                        case "/sample2":
                            val values2 = request.getHeaders().values(HEADER_NAME);
                            assertEquals(2, values2.size());
                            assertEquals("class", values2.get(0));
                            assertEquals("method", values2.get(1));
                            return new MockResponse().setBody(CONTENT);
                        case "/sample3":
                            val values3 = request.getHeaders().values(HEADER_NAME);
                            if (values3.size() == 1) {
                                assertEquals("method", values3.get(0));
                            } else if (values3.size() == 2) {
                                assertEquals("method", values3.get(0));
                                assertEquals("parameter", values3.get(1));
                            }
                            assertEquals(BODY, request.getBody().readUtf8());
                            return new MockResponse().setBody(CONTENT);
                        default:
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            });
            mockWebServer.start(41220);

            val client = ohLoader.getClient(InterceptorClient.class);
            assertEquals(CONTENT, client.sample1());
            assertEquals(CONTENT, client.sample2());
            assertEquals(CONTENT, client.sample3(new ParamInterceptor(), Level.BODY, BODY));
            assertEquals(CONTENT, client.sample3(null, Level.BODY, BODY));

            val providerClient = ohLoader.getClient(InterceptorProviderClient.class);
            assertEquals(CONTENT, providerClient.sample1());
            assertEquals(CONTENT, providerClient.sample2());
            assertEquals(CONTENT, providerClient.sample3(new ParamInterceptor(), Level.BODY, BODY));
            assertEquals(CONTENT, providerClient.sample3(null, Level.BODY, BODY));

            val clientNeo = ohLoader.getClient(InterceptorClientNeo.class);
            assertEquals(CONTENT, clientNeo.sample1());
            assertEquals(CONTENT, clientNeo.sample2());
            assertEquals(CONTENT, clientNeo.sample3(new ParamInterceptor(), Level.BODY, BODY));
            assertEquals(CONTENT, clientNeo.sample3(null, Level.BODY, BODY));
        }
    }

    @SneakyThrows
    @Test
    public void testError() {
        assertThrows(ProviderException.class, () ->
                ohLoader.getClient(ErrorClassLoggingClient.class));
        assertThrows(ProviderException.class, () ->
                ohLoader.getClient(ErrorClassInterceptorClient.class));

        val loggingClient = ohLoader.getClient(ErrorMethodLoggingClient.class);
        assertThrows(ProviderException.class, loggingClient::sample);
        val interceptorClient = ohLoader.getClient(ErrorMethodInterceptorClient.class);
        assertThrows(ProviderException.class, interceptorClient::sample);
    }

    @OhClient
    @Mapping("${root}:${port}")
    @ClientInterceptor
    @ClientInterceptor(ClassInterceptor.class)
    @ClientLoggingLevel(Level.BASIC)
    public interface InterceptorClient {

        String sample1();

        @ClientInterceptor
        @ClientInterceptor(MethodInterceptor.class)
        @ClientLoggingLevel(Level.HEADERS)
        String sample2();

        @RequestMethod(HttpMethod.POST)
        @ClientInterceptorCleanup
        @ClientInterceptor(MethodInterceptor.class)
        @ClientLoggingLevel(Level.HEADERS)
        String sample3(ParamInterceptor interceptor, Level level, @RequestBodyRaw String body);
    }

    @OhClient
    @Mapping("${root}:${port}")
    @ClientInterceptor(provider = TestInterceptorProvider.class)
    @ClientLoggingLevel(provider = TestLoggingLevelProvider.class)
    public interface InterceptorProviderClient {

        String sample1();

        @ClientInterceptor(provider = TestInterceptorProvider.class)
        @ClientLoggingLevel(provider = TestLoggingLevelProvider.class)
        String sample2();

        @RequestMethod(HttpMethod.POST)
        @ClientInterceptorCleanup
        @ClientInterceptor(provider = TestInterceptorProvider.class)
        @ClientLoggingLevel(provider = TestLoggingLevelProvider.class)
        String sample3(ParamInterceptor interceptor, Level level, @RequestBodyRaw String body);
    }

    @OhClient
    @Mapping("${root}:${port}")
    @ClientLoggingLevel(provider = ErrorClassLoggingLevelProvider.class)
    public interface ErrorClassLoggingClient {}

    @OhClient
    @Mapping("${root}:${port}")
    @ClientLoggingLevel(provider = TestLoggingLevelProvider.class)
    public interface ErrorMethodLoggingClient {

        @ClientLoggingLevel(provider = ErrorMethodLoggingLevelProvider.class)
        String sample();
    }

    @OhClient
    @Mapping("${root}:${port}")
    @ClientInterceptor(provider = ErrorClassInterceptorProvider.class)
    public interface ErrorClassInterceptorClient {}

    @OhClient
    @Mapping("${root}:${port}")
    @ClientInterceptor(provider = TestInterceptorProvider.class)
    public interface ErrorMethodInterceptorClient {

        @ClientInterceptor(provider = ErrorMethodInterceptorProvider.class)
        String sample();
    }

    public static class TestLoggingLevelProvider implements LoggingLevelProvider {

        @Override
        public Level level(Class<?> clazz) {
            return Level.BASIC;
        }

        @Override
        public Level level(Class<?> clazz, Method method) {
            return Level.HEADERS;
        }
    }

    @EverythingIsNonNull
    public static class ClassInterceptor implements Interceptor {

        @Nonnull
        @Override
        public Response intercept(@Nonnull Chain chain) throws IOException {
            val requestBuilder = chain.request().newBuilder();
            requestBuilder.addHeader(HEADER_NAME, "class");
            return chain.proceed(requestBuilder.build());
        }
    }

    public static class TestInterceptorProvider implements InterceptorProvider {

        @Override
        public Interceptor interceptor(Class<?> clazz) {
            return new ClassInterceptor();
        }

        @Override
        public Interceptor interceptor(Class<?> clazz, Method method) {
            return new MethodInterceptor();
        }
    }

    @EverythingIsNonNull
    public static class MethodInterceptor implements Interceptor {

        @Nonnull
        @Override
        public Response intercept(@Nonnull Chain chain) throws IOException {
            val requestBuilder = chain.request().newBuilder();
            requestBuilder.addHeader(HEADER_NAME, "method");
            return chain.proceed(requestBuilder.build());
        }
    }

    @EverythingIsNonNull
    public static class ParamInterceptor implements Interceptor {

        @Nonnull
        @Override
        public Response intercept(@Nonnull Chain chain) throws IOException {
            val requestBuilder = chain.request().newBuilder();
            requestBuilder.addHeader(HEADER_NAME, "parameter");
            return chain.proceed(requestBuilder.build());
        }
    }

    public static class ErrorClassLoggingLevelProvider implements LoggingLevelProvider {}

    public static class ErrorMethodLoggingLevelProvider implements LoggingLevelProvider {

        @Override
        public Level level(Class<?> clazz) {
            return Level.BASIC;
        }
    }

    public static class ErrorClassInterceptorProvider implements InterceptorProvider {}

    public static class ErrorMethodInterceptorProvider implements InterceptorProvider {

        @Override
        public Interceptor interceptor(Class<?> clazz) {
            return new ClassInterceptor();
        }
    }

    @OhClient
    @Mapping("${root}:${port}")
    @ConfigureWith(InterceptorClientConfig.class)
    public interface InterceptorClientNeo {

        String sample1();

        @ConfigureWith(Sample2Config.class)
        String sample2();

        @ConfigureWith(Sample3Config.class)
        String sample3(ParamInterceptor interceptor, Level level, @RequestBodyRaw String body);
    }

    public static class InterceptorClientConfig implements ClientInterceptorsConfigurer, ClientLoggingLevelConfigurer {

        @Override
        public List<Interceptor> interceptors() {
            return newArrayList(new ClassInterceptor());
        }

        @Override
        public Level loggingLevel() {
            return Level.BASIC;
        }
    }

    public static class Sample2Config implements ClientInterceptorsConfigurer, ClientLoggingLevelConfigurer {

        @Override
        public List<Interceptor> interceptors() {
            return newArrayList(new MethodInterceptor());
        }

        @Override
        public Level loggingLevel() {
            return Level.HEADERS;
        }
    }

    public static class Sample3Config extends Sample2Config implements RequestMethodConfigurer, ClientInterceptorsCleanupConfigurer {

        @Override
        public HttpMethod requestMethod() {
            return HttpMethod.POST;
        }
    }
}
