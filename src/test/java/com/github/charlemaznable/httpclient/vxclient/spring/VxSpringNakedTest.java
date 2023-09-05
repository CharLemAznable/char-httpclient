package com.github.charlemaznable.httpclient.vxclient.spring;

import com.github.charlemaznable.httpclient.common.spring.CommonSpringNakedTest;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientConcrete;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientMocked;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientNone;
import com.github.charlemaznable.httpclient.vxclient.VxException;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.httpclient.vxclient.VxFactory.getClient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(VertxExtension.class)
@SpringJUnitConfig(VxSpringNakedConfiguration.class)
public class VxSpringNakedTest extends CommonSpringNakedTest {

    @Test
    public void testVxClientNaked(VertxTestContext test) {
        startMockWebServer();

        assertThrows(VxException.class,
                () -> getClient(TestHttpClientConcrete.class));

        assertThrows(VxException.class,
                () -> getClient(TestHttpClientNone.class));

        val testHttpClientMocked = getClient(TestHttpClientMocked.class);
        Future.all(newArrayList(
                testHttpClientMocked.vxSample().onSuccess(response -> test.verify(() -> assertEquals("SampleError", response))),
                testHttpClientMocked.vxSampleWrapper().onSuccess(response -> test.verify(() -> assertEquals("[SampleError]", response)))
        )).onComplete(result -> {
            shutdownMockWebServer();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }
}
