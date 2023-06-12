package com.github.charlemaznable.httpclient.wfclient;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonConfigurerTest;
import lombok.val;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigurerTest extends CommonConfigurerTest {

    @Test
    public void testConfigurer() {
        startMockWebServer();

        val wfLoader = WfFactory.wfLoader(reflectFactory());

        val client = wfLoader.getClient(ConfigurerClient.class);
        assertEquals("SAMPLE", client.sample().block());
        try {
            client.sample2().block();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Connection refused: /127.0.0.1:41311"));
        }

        val clientError = wfLoader.getClient(ConfigurerClientError.class);
        assertEquals("SAMPLE", clientError.sample().block());

        shutdownMockWebServer();
    }

    @WfClient
    @ConfigureWith(ConfigurerClientConfig.class)
    public interface ConfigurerClient {

        @ConfigureWith(ConfigurerClientSampleConfig.class)
        Mono<String> sample();

        @ConfigureWith(ConfigurerClientSample2Config.class)
        Mono<Void> sample2();
    }

    @WfClient
    @ConfigureWith(ConfigurerClientErrorConfig.class)
    @Mapping("${root}:41310")
    public interface ConfigurerClientError {

        @ConfigureWith(ConfigurerClientSampleErrorConfig.class)
        Mono<String> sample();
    }
}
