package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.common.ConfigureWith;
import com.github.charlemaznable.httpclient.common.FixedPathVar;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.common.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.common.PathVar;
import com.github.charlemaznable.httpclient.configurer.FixedPathVarsConfigurer;
import com.github.charlemaznable.httpclient.configurer.MappingMethodNameDisabledConfigurer;
import com.github.charlemaznable.httpclient.ohclient.OhFactory.OhLoader;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.util.List;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
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
                    return switch (requireNonNull(request.getPath())) {
                        case "/V1/V2" -> new MockResponse().setBody("V2");
                        case "/V1/V3" -> new MockResponse().setBody("V3");
                        case "/V1/V4" -> new MockResponse().setBody("V4");
                        default -> new MockResponse()
                                .setResponseCode(HttpStatus.NOT_FOUND.value())
                                .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    };
                }
            });
            mockWebServer.start(41150);

            val httpClient = ohLoader.getClient(PathVarHttpClient.class);
            assertEquals("V2", httpClient.sampleDefault());
            assertEquals("V3", httpClient.sampleMapping());
            assertEquals("V4", httpClient.samplePathVars("V4"));

            val httpClientNeo = ohLoader.getClient(PathVarHttpClientNeo.class);
            assertEquals("V2", httpClientNeo.sampleDefault());
            assertEquals("V3", httpClientNeo.sampleMapping());
            assertEquals("V4", httpClientNeo.samplePathVars("V4"));
        }
    }

    @FixedPathVar(name = "P1", value = "V1")
    @FixedPathVar(name = "P2", value = "V2")
    @Mapping("${root}:41150/{P1}/{P2}")
    @MappingMethodNameDisabled
    @OhClient
    public interface PathVarHttpClient {

        String sampleDefault();

        @FixedPathVar(name = "P2", value = "V3")
        String sampleMapping();

        @FixedPathVar(name = "P2", value = "V3")
        String samplePathVars(@PathVar("P2") String v4);
    }

    @Mapping("${root}:41150/{P1}/{P2}")
    @OhClient
    @ConfigureWith(PathVarHttpClientConfig.class)
    public interface PathVarHttpClientNeo {

        String sampleDefault();

        @ConfigureWith(SampleMappingConfig.class)
        String sampleMapping();

        @ConfigureWith(SampleMappingConfig.class)
        String samplePathVars(@PathVar("P2") String v4);
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
