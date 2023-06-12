package com.github.charlemaznable.httpclient.vxclient.spring;

import com.github.charlemaznable.core.spring.SpringContext;
import com.github.charlemaznable.httpclient.common.spring.CommonSpringErrorTest;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientConcrete;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientMocked;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientNone;
import io.vertx.core.CompositeFuture;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(VertxExtension.class)
@SpringJUnitConfig(VxSpringErrorConfiguration.class)
public class VxSpringErrorTest extends CommonSpringErrorTest {

    @Test
    public void testVxClientError(VertxTestContext test) {
        startMockWebServer();

        val testHttpClientConcrete = SpringContext.getBean(TestHttpClientConcrete.class);
        assertNull(testHttpClientConcrete);

        val testHttpClientNone = SpringContext.getBean(TestHttpClientNone.class);
        assertNull(testHttpClientNone);

        val testHttpClientMocked = SpringContext.getBean(TestHttpClientMocked.class);
        CompositeFuture.all(newArrayList(
                testHttpClientMocked.vxSample().onSuccess(response -> test.verify(() -> assertEquals("SampleError", response))),
                testHttpClientMocked.vxSampleWrapper().onSuccess(response -> test.verify(() -> assertEquals("[SampleError]", response)))
        )).onComplete(result -> {
            shutdownMockWebServer();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }
}
