package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.annotation.Bundle;
import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.ContentFormat;
import com.github.charlemaznable.httpclient.annotation.FixedParameter;
import com.github.charlemaznable.httpclient.annotation.FixedPathVar;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.Parameter;
import com.github.charlemaznable.httpclient.annotation.RequestBodyRaw;
import com.github.charlemaznable.httpclient.annotation.RequestMethod;
import com.github.charlemaznable.httpclient.common.CommonParameterTest;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Mapp.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParameterTest extends CommonParameterTest {

    @Test
    public void testParameterGet() {
        startGetMockWebServer();

        val ohLoader = OhFactory.ohLoader(reflectFactory());

        val httpClient = ohLoader.getClient(GetParameterHttpClient.class);
        assertEquals("OK", httpClient.sampleDefault());
        assertEquals("OK", httpClient.sampleMapping());
        assertEquals("OK", httpClient.sampleParameters(null, "V4"));
        assertEquals("OK", httpClient.sampleBundle(new TestBundle(null, null, "V4", "V5")));
        assertEquals("OK", httpClient.sampleBundle2(null));
        assertEquals("OK", httpClient.sampleBundle3(of("T2", null, "T4", "V4", "t5", "V5")));

        val httpClientNeo = ohLoader.getClient(GetParameterHttpClientNeo.class);
        assertEquals("OK", httpClientNeo.sampleDefault());
        assertEquals("OK", httpClientNeo.sampleMapping());
        assertEquals("OK", httpClientNeo.sampleParameters(null, "V4"));
        assertEquals("OK", httpClientNeo.sampleBundle(new TestBundle(null, null, "V4", "V5")));
        assertEquals("OK", httpClientNeo.sampleBundle2(null));
        assertEquals("OK", httpClientNeo.sampleBundle3(of("T2", null, "T4", "V4", "t5", "V5")));

        shutdownGetMockWebServer();
    }

    @Test
    public void testParameterPost() {
        startPostMockWebServer();

        val ohLoader = OhFactory.ohLoader(reflectFactory());

        val httpClient = ohLoader.getClient(PostParameterHttpClient.class);
        assertEquals("OK", httpClient.sampleDefault());
        assertEquals("OK", httpClient.sampleMapping());
        assertEquals("OK", httpClient.sampleParameters(null, "V4"));
        assertEquals("OK", httpClient.sampleBundle(new TestBundle(null, null, "V4", "V5")));
        assertEquals("OK", httpClient.sampleBundle2(null));
        assertEquals("OK", httpClient.sampleBundle3(of("T2", null, "T4", "V4", "t5", "V5")));
        assertEquals("OK", httpClient.sampleRaw("T3=V3&T4=V4"));
        assertEquals("OK", httpClient.sampleRawError(new Object()));

        val httpClientNeo = ohLoader.getClient(PostParameterHttpClientNeo.class);
        assertEquals("OK", httpClientNeo.sampleDefault());
        assertEquals("OK", httpClientNeo.sampleMapping());
        assertEquals("OK", httpClientNeo.sampleParameters(null, "V4"));
        assertEquals("OK", httpClientNeo.sampleBundle(new TestBundle(null, null, "V4", "V5")));
        assertEquals("OK", httpClientNeo.sampleBundle2(null));
        assertEquals("OK", httpClientNeo.sampleBundle3(of("T2", null, "T4", "V4", "t5", "V5")));
        assertEquals("OK", httpClientNeo.sampleRaw("T3=V3&T4=V4"));
        assertEquals("OK", httpClientNeo.sampleRawError(new Object()));

        shutdownPostMockWebServer();
    }

    @FixedPathVar(name = "T0", value = "V0")
    @FixedParameter(name = "T1", value = "V1")
    @FixedParameter(name = "T2", value = "V2")
    @Mapping("${root}:41160")
    @OhClient
    public interface GetParameterHttpClient {

        String sampleDefault();

        @Mapping("/sampleMapping?T0={T0}")
        @FixedParameter(name = "T2")
        @FixedParameter(name = "T3", value = "V3")
        String sampleMapping();

        @Mapping("/sampleParameters?T0={T0}")
        @FixedParameter(name = "T2")
        @FixedParameter(name = "T3", value = "V3")
        String sampleParameters(@Parameter("T3") String v3,
                                @Parameter("T4") String v4);

        String sampleBundle(@Bundle TestBundle bundle);

        String sampleBundle2(@Bundle TestBundle bundle);

        String sampleBundle3(@Bundle Map<String, Object> bundle);
    }

    @FixedParameter(name = "T1", value = "V1")
    @FixedParameter(name = "T2", value = "V2")
    @RequestMethod(HttpMethod.POST)
    @ContentFormat(ContentFormat.FormContentFormatter.class)
    @Mapping("${root}:41161")
    @OhClient
    public interface PostParameterHttpClient {

        String sampleDefault();

        @ContentFormat(ContentFormat.JsonContentFormatter.class)
        @FixedParameter(name = "T2")
        @FixedParameter(name = "T3", value = "V3")
        String sampleMapping();

        @ContentFormat(ContentFormat.TextXmlContentFormatter.class)
        @FixedParameter(name = "T2")
        @FixedParameter(name = "T3", value = "V3")
        String sampleParameters(@Parameter("T3") String v3,
                                @Parameter("T4") String v4);

        String sampleBundle(@Bundle TestBundle bundle);

        String sampleBundle2(@Bundle TestBundle bundle);

        String sampleBundle3(@Bundle Map<String, Object> bundle);

        String sampleRaw(@RequestBodyRaw String raw);

        String sampleRawError(@RequestBodyRaw Object raw);
    }

    @FixedPathVar(name = "T0", value = "V0")
    @Mapping("${root}:41160")
    @OhClient
    @ConfigureWith(ParameterHttpClientConfig.class)
    public interface GetParameterHttpClientNeo {

        String sampleDefault();

        @Mapping("/sampleMapping?T0={T0}")
        @ConfigureWith(SampleMappingConfig.class)
        String sampleMapping();

        @Mapping("/sampleParameters?T0={T0}")
        @ConfigureWith(SampleMappingConfig.class)
        String sampleParameters(@Parameter("T3") String v3,
                                @Parameter("T4") String v4);

        String sampleBundle(@Bundle TestBundle bundle);

        String sampleBundle2(@Bundle TestBundle bundle);

        String sampleBundle3(@Bundle Map<String, Object> bundle);
    }

    @RequestMethod(HttpMethod.POST)
    @Mapping("${root}:41161")
    @OhClient
    @ConfigureWith(ParameterHttpClientConfig.class)
    public interface PostParameterHttpClientNeo {

        String sampleDefault();

        @ConfigureWith(SampleMappingConfig.class)
        String sampleMapping();

        @ConfigureWith(SampleParametersConfig.class)
        String sampleParameters(@Parameter("T3") String v3,
                                @Parameter("T4") String v4);

        String sampleBundle(@Bundle TestBundle bundle);

        String sampleBundle2(@Bundle TestBundle bundle);

        String sampleBundle3(@Bundle Map<String, Object> bundle);

        String sampleRaw(@RequestBodyRaw String raw);

        String sampleRawError(@RequestBodyRaw Object raw);
    }
}
