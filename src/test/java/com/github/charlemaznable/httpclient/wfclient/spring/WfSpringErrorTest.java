package com.github.charlemaznable.httpclient.wfclient.spring;

import com.github.charlemaznable.core.spring.SpringContext;
import com.github.charlemaznable.httpclient.common.spring.CommonSpringErrorTest;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientConcrete;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientMocked;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientNone;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringJUnitConfig(WfSpringErrorConfiguration.class)
public class WfSpringErrorTest extends CommonSpringErrorTest {

    @Test
    public void testWfClientError() {
        startMockWebServer();

        val testHttpClientIsolated = SpringContext.getBean(TestHttpClientMocked.class);
        assertEquals("SampleError", testHttpClientIsolated.wfSample().block());
        assertEquals("[SampleError]", testHttpClientIsolated.wfSampleWrapper().block());

        val testHttpClientConcrete = SpringContext.getBean(TestHttpClientConcrete.class);
        assertNull(testHttpClientConcrete);

        val testHttpClientNone = SpringContext.getBean(TestHttpClientNone.class);
        assertNull(testHttpClientNone);

        shutdownMockWebServer();
    }
}
