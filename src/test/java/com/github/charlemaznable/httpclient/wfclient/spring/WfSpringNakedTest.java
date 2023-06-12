package com.github.charlemaznable.httpclient.wfclient.spring;

import com.github.charlemaznable.httpclient.common.spring.CommonSpringNakedTest;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientConcrete;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientMocked;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientNone;
import com.github.charlemaznable.httpclient.wfclient.WfException;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static com.github.charlemaznable.httpclient.wfclient.WfFactory.getClient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringJUnitConfig(WfSpringNakedConfiguration.class)
public class WfSpringNakedTest extends CommonSpringNakedTest {

    @Test
    public void testWfClientNaked() {
        startMockWebServer();

        val testHttpClientIsolated = getClient(TestHttpClientMocked.class);
        assertEquals("SampleError", testHttpClientIsolated.wfSample().block());
        assertEquals("[SampleError]", testHttpClientIsolated.wfSampleWrapper().block());

        assertThrows(WfException.class,
                () -> getClient(TestHttpClientConcrete.class));

        assertThrows(WfException.class,
                () -> getClient(TestHttpClientNone.class));

        shutdownMockWebServer();
    }
}
