package com.github.charlemaznable.httpclient.vxclient.spring;

import com.github.charlemaznable.core.spring.SpringContext;
import com.github.charlemaznable.httpclient.common.spring.CommonSpringTest;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientConcrete;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientMocked;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientNone;
import com.github.charlemaznable.httpclient.common.testclient2.TestHttpClientUnscanned;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.util.MockUtil;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
@SpringJUnitConfig(VxSpringConfiguration.class)
public class VxSpringTest extends CommonSpringTest {

    @Test
    public void testVxClient(VertxTestContext test) {
        startMockWebServer();

        val testHttpClientConcrete = SpringContext.getBean(TestHttpClientConcrete.class);
        assertNull(testHttpClientConcrete);

        val testHttpClientNone = SpringContext.getBean(TestHttpClientNone.class);
        assertNull(testHttpClientNone);

        assertNotNull(SpringContext.getBean(TestHttpClientUnscanned.class));

        val testHttpClientMocked = SpringContext.getBean(TestHttpClientMocked.class);
        assertTrue(MockUtil.isSpy(testHttpClientMocked));
        Future.all(newArrayList(
                testHttpClientMocked.vxSample().onSuccess(response -> test.verify(() -> assertEquals(SAMPLE, response))),
                testHttpClientMocked.vxSampleWrapper().onSuccess(response -> test.verify(() -> assertEquals("[Sample]", response)))
        )).onComplete(result -> {
            shutdownMockWebServer();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }
}
