package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.ContentFormat;
import com.github.charlemaznable.httpclient.annotation.ExtraUrlQuery;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingBalance;
import com.github.charlemaznable.httpclient.annotation.Parameter;
import com.github.charlemaznable.httpclient.annotation.RequestMethod;
import com.github.charlemaznable.httpclient.common.CommonExtraQueryTest;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExtraQueryTest extends CommonExtraQueryTest {

    @Test
    public void testExtraQuery() {
        startMockWebServer();

        val ohLoader = OhFactory.ohLoader(reflectFactory());

        val httpClient = ohLoader.getClient(ExtraHttpClient.class);
        assertEquals("OK", httpClient.sampleGet("PV1"));
        assertEquals("OK", httpClient.samplePost("PV1"));
        assertEquals("OK", httpClient.samplePost("PV1", new ExtraOnMethod()));
        assertEquals("OK", httpClient.sampleNone("PV1"));

        val httpClientNeo = ohLoader.getClient(ExtraHttpClientNeo.class);
        assertEquals("OK", httpClientNeo.sampleGet("PV1"));
        assertEquals("OK", httpClientNeo.samplePost("PV1"));
        assertEquals("OK", httpClientNeo.sampleNone("PV1"));

        shutdownMockWebServer();
    }

    @MappingBalance(MappingBalance.RoundRobinBalancer.class)
    @ExtraUrlQuery(ExtraOnClass.class)
    @ContentFormat(ContentFormat.JsonContentFormatter.class)
    @Mapping("${root}:41230")
    @OhClient
    public interface ExtraHttpClient {

        String sampleGet(@Parameter("P1") String p);

        @RequestMethod(HttpMethod.POST)
        @ExtraUrlQuery(ExtraOnMethod.class)
        String samplePost(@Parameter("P1") String p);

        @RequestMethod(HttpMethod.POST)
        String samplePost(@Parameter("P1") String p, ExtraUrlQuery.ExtraUrlQueryBuilder extraUrlQueryBuilder);

        @ExtraUrlQuery.Disabled
        String sampleNone(@Parameter("P1") String p);
    }

    @MappingBalance(MappingBalance.RoundRobinBalancer.class)
    @ContentFormat(ContentFormat.JsonContentFormatter.class)
    @Mapping("${root}:41230")
    @OhClient
    @ConfigureWith(ExtraOnClassConfig.class)
    public interface ExtraHttpClientNeo {

        String sampleGet(@Parameter("P1") String p);

        @RequestMethod(HttpMethod.POST)
        @ConfigureWith(ExtraOnMethodConfig.class)
        String samplePost(@Parameter("P1") String p);

        @ConfigureWith(ExtraNoneConfig.class)
        String sampleNone(@Parameter("P1") String p);
    }
}
