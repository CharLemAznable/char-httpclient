package com.github.charlemaznable.httpclient.wfclient.spring;

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

@SpringJUnitConfig(WfSpringConfiguration.class)
public class WfSpringTest extends CommonSpringTest {

    @Test
    public void testWfClient() {
        startMockWebServer();

        val testHttpClientMocked = SpringContext.getBean(TestHttpClientMocked.class);
        assertEquals(SAMPLE, testHttpClientMocked.wfSample().block());
        assertEquals("[Sample]", testHttpClientMocked.wfSampleWrapper().block());
        assertTrue(MockUtil.isSpy(testHttpClientMocked));

        val testHttpClientConcrete = SpringContext.getBean(TestHttpClientConcrete.class);
        assertNull(testHttpClientConcrete);

        val testHttpClientNone = SpringContext.getBean(TestHttpClientNone.class);
        assertNull(testHttpClientNone);

        assertNotNull(SpringContext.getBean(TestHttpClientUnscanned.class));

        shutdownMockWebServer();
    }
}
