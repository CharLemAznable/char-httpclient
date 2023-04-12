package com.github.charlemaznable.httpclient.ohclient.spring;

import com.github.charlemaznable.httpclient.common.spring.CommonSpringNakedTest;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientConcrete;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientMocked;
import com.github.charlemaznable.httpclient.common.testclient.TestHttpClientNone;
import com.github.charlemaznable.httpclient.ohclient.OhException;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static com.github.charlemaznable.httpclient.ohclient.OhFactory.getClient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringJUnitConfig(OhSpringNakedConfiguration.class)
public class OhSpringNakedTest extends CommonSpringNakedTest {

    @Test
    public void testOhClientNaked() {
        startMockWebServer();

        val testHttpClientIsolated = getClient(TestHttpClientMocked.class);
        assertEquals("SampleError", testHttpClientIsolated.ohSample());
        assertEquals("[SampleError]", testHttpClientIsolated.ohSampleWrapper());

        assertThrows(OhException.class,
                () -> getClient(TestHttpClientConcrete.class));

        assertThrows(OhException.class,
                () -> getClient(TestHttpClientNone.class));

        shutdownMockWebServer();
    }
}
