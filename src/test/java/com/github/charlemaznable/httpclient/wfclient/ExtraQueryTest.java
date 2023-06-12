package com.github.charlemaznable.httpclient.wfclient;

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
import reactor.core.publisher.Mono;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExtraQueryTest extends CommonExtraQueryTest {

    @Test
    public void testExtraQuery() {
        startMockWebServer();

        val wfLoader = WfFactory.wfLoader(reflectFactory());

        val httpClient = wfLoader.getClient(ExtraHttpClient.class);
        assertEquals("OK", httpClient.sampleGet("PV1").block());
        assertEquals("OK", httpClient.samplePost("PV1").block());
        assertEquals("OK", httpClient.samplePost("PV1", new ExtraOnMethod()).block());
        assertEquals("OK", httpClient.sampleNone("PV1").block());

        val httpClientNeo = wfLoader.getClient(ExtraHttpClientNeo.class);
        assertEquals("OK", httpClientNeo.sampleGet("PV1").block());
        assertEquals("OK", httpClientNeo.samplePost("PV1").block());
        assertEquals("OK", httpClientNeo.sampleNone("PV1").block());

        shutdownMockWebServer();
    }

    @MappingBalance(MappingBalance.RoundRobinBalancer.class)
    @ExtraUrlQuery(ExtraOnClass.class)
    @ContentFormat(ContentFormat.JsonContentFormatter.class)
    @Mapping("${root}:41230")
    @WfClient
    public interface ExtraHttpClient {

        Mono<String> sampleGet(@Parameter("P1") String p);

        @RequestMethod(HttpMethod.POST)
        @ExtraUrlQuery(ExtraOnMethod.class)
        Mono<String> samplePost(@Parameter("P1") String p);

        @RequestMethod(HttpMethod.POST)
        Mono<String> samplePost(@Parameter("P1") String p, ExtraUrlQuery.ExtraUrlQueryBuilder extraUrlQueryBuilder);

        @ExtraUrlQuery.Disabled
        Mono<String> sampleNone(@Parameter("P1") String p);
    }

    @MappingBalance(MappingBalance.RoundRobinBalancer.class)
    @ContentFormat(ContentFormat.JsonContentFormatter.class)
    @Mapping("${root}:41230")
    @WfClient
    @ConfigureWith(ExtraOnClassConfig.class)
    public interface ExtraHttpClientNeo {

        Mono<String> sampleGet(@Parameter("P1") String p);

        @RequestMethod(HttpMethod.POST)
        @ConfigureWith(ExtraOnMethodConfig.class)
        Mono<String> samplePost(@Parameter("P1") String p);

        @ConfigureWith(ExtraNoneConfig.class)
        Mono<String> sampleNone(@Parameter("P1") String p);
    }
}
