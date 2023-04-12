package com.github.charlemaznable.httpclient.ohclient.spring;

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

@SpringJUnitConfig(OhSpringErrorConfiguration.class)
public class OhSpringErrorTest extends CommonSpringErrorTest {

    @Test
    public void testOhClientError() {
        startMockWebServer();

        val testHttpClientIsolated = SpringContext.getBean(TestHttpClientMocked.class);
        assertEquals("SampleError", testHttpClientIsolated.ohSample());
        assertEquals("[SampleError]", testHttpClientIsolated.ohSampleWrapper());

        val testHttpClientConcrete = SpringContext.getBean(TestHttpClientConcrete.class);
        assertNull(testHttpClientConcrete);

        val testHttpClientNone = SpringContext.getBean(TestHttpClientNone.class);
        assertNull(testHttpClientNone);

        shutdownMockWebServer();
    }
}
