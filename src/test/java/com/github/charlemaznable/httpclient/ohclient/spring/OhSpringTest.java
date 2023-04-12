package com.github.charlemaznable.httpclient.ohclient.spring;

import com.github.charlemaznable.core.spring.SpringContext;
import com.github.charlemaznable.httpclient.common.spring.CommonSpringTest;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientConcrete;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientMocked;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientNone;
import com.github.charlemaznable.httpclient.common.testclient2.TestHttpClientUnscanned;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.MockUtil;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(OhSpringConfiguration.class)
public class OhSpringTest extends CommonSpringTest {

    @Test
    public void testOhClient() {
        startMockWebServer();

        val testHttpClientMocked = SpringContext.getBean(TestHttpClientMocked.class);
        assertEquals(SAMPLE, testHttpClientMocked.ohSample());
        assertEquals("[Sample]", testHttpClientMocked.ohSampleWrapper());
        assertTrue(MockUtil.isSpy(testHttpClientMocked));

        val testHttpClientConcrete = SpringContext.getBean(TestHttpClientConcrete.class);
        assertNull(testHttpClientConcrete);

        val testHttpClientNone = SpringContext.getBean(TestHttpClientNone.class);
        assertNull(testHttpClientNone);

        assertNotNull(SpringContext.getBean(TestHttpClientUnscanned.class));

        shutdownMockWebServer();
    }
}
