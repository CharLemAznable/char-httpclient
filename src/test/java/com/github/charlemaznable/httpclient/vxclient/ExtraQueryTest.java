package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.ContentFormat;
import com.github.charlemaznable.httpclient.annotation.ExtraUrlQuery;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingBalance;
import com.github.charlemaznable.httpclient.annotation.Parameter;
import com.github.charlemaznable.httpclient.annotation.RequestMethod;
import com.github.charlemaznable.httpclient.common.CommonExtraQueryTest;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class ExtraQueryTest extends CommonExtraQueryTest {

    @Test
    public void testExtraQuery(Vertx vertx, VertxTestContext test) {
        startMockWebServer();

        val vxLoader = VxFactory.vxLoader(new VertxReflectFactory(vertx));

        val httpClient = vxLoader.getClient(ExtraHttpClient.class);
        val httpClientNeo = vxLoader.getClient(ExtraHttpClientNeo.class);

        Future.all(newArrayList(
                httpClient.sampleGet("PV1").onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClient.samplePost("PV1").onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClient.samplePost("PV1", new ExtraOnMethod()).onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClient.sampleNone("PV1").onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClientNeo.sampleGet("PV1").onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClientNeo.samplePost("PV1").onSuccess(response -> test.verify(() -> assertEquals("OK", response))),
                httpClientNeo.sampleNone("PV1").onSuccess(response -> test.verify(() -> assertEquals("OK", response)))
        )).onComplete(result -> {
            shutdownMockWebServer();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    @MappingBalance(MappingBalance.RoundRobinBalancer.class)
    @ExtraUrlQuery(ExtraOnClass.class)
    @ContentFormat(ContentFormat.JsonContentFormatter.class)
    @Mapping("${root}:41230")
    @VxClient
    public interface ExtraHttpClient {

        Future<String> sampleGet(@Parameter("P1") String p);

        @RequestMethod(HttpMethod.POST)
        @ExtraUrlQuery(ExtraOnMethod.class)
        Future<String> samplePost(@Parameter("P1") String p);

        @RequestMethod(HttpMethod.POST)
        Future<String> samplePost(@Parameter("P1") String p, ExtraUrlQuery.ExtraUrlQueryBuilder extraUrlQueryBuilder);

        @ExtraUrlQuery.Disabled
        Future<String> sampleNone(@Parameter("P1") String p);
    }

    @MappingBalance(MappingBalance.RoundRobinBalancer.class)
    @ContentFormat(ContentFormat.JsonContentFormatter.class)
    @Mapping("${root}:41230")
    @VxClient
    @ConfigureWith(ExtraOnClassConfig.class)
    public interface ExtraHttpClientNeo {

        Future<String> sampleGet(@Parameter("P1") String p);

        @RequestMethod(HttpMethod.POST)
        @ConfigureWith(ExtraOnMethodConfig.class)
        Future<String> samplePost(@Parameter("P1") String p);

        @ConfigureWith(ExtraNoneConfig.class)
        Future<String> sampleNone(@Parameter("P1") String p);
    }
}
