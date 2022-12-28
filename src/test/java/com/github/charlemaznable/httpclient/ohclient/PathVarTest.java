package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.common.FixedPathVar;
import com.github.charlemaznable.httpclient.common.FixedValueProvider;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.common.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.common.PathVar;
import com.github.charlemaznable.httpclient.ohclient.OhFactory.OhLoader;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathVarTest {

    private static final OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @SneakyThrows
    @Test
    public void testOhPathVar() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Nonnull
                @Override
                public MockResponse dispatch(@Nonnull RecordedRequest request) {
                    switch (requireNonNull(request.getPath())) {
                        case "/V1/V2":
                            return new MockResponse().setBody("V2");
                        case "/V1/V3":
                            return new MockResponse().setBody("V3");
                        case "/V1/V4":
                            return new MockResponse().setBody("V4");
                        default:
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            });
            mockWebServer.start(41150);

            val httpClient = ohLoader.getClient(PathVarHttpClient.class);
            assertEquals("V2", httpClient.sampleDefault());
            assertEquals("V3", httpClient.sampleMapping());
            assertEquals("V4", httpClient.samplePathVars("V4"));
        }
    }

    @FixedPathVar(name = "P1", value = "V1")
    @FixedPathVar(name = "P2", valueProvider = P2Provider.class)
    @Mapping("${root}:41150/{P1}/{P2}")
    @MappingMethodNameDisabled
    @OhClient
    public interface PathVarHttpClient {

        String sampleDefault();

        @FixedPathVar(name = "P2", valueProvider = P2Provider.class)
        String sampleMapping();

        @FixedPathVar(name = "P2", value = "V3")
        String samplePathVars(@PathVar("P2") String v4);
    }

    public static class P2Provider implements FixedValueProvider {

        @Override
        public String value(Class<?> clazz, String name) {
            return "V2";
        }

        @Override
        public String value(Class<?> clazz, Method method, String name) {
            return "V3";
        }
    }
}
