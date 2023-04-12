package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonConfigurerTest;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigurerTest extends CommonConfigurerTest {

    @Test
    public void testConfigurer() {
        startMockWebServer();

        val ohLoader = OhFactory.ohLoader(reflectFactory());

        val client = ohLoader.getClient(ConfigurerClient.class);
        assertEquals("SAMPLE", client.sample());
        try {
            client.sample2();
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41311", e.getMessage());
        }

        val clientError = ohLoader.getClient(ConfigurerClientError.class);
        assertEquals("SAMPLE", clientError.sample());

        shutdownMockWebServer();
    }

    @OhClient
    @ConfigureWith(ConfigurerClientConfig.class)
    public interface ConfigurerClient {

        @ConfigureWith(ConfigurerClientSampleConfig.class)
        String sample();

        @ConfigureWith(ConfigurerClientSample2Config.class)
        void sample2();
    }

    @OhClient
    @ConfigureWith(ConfigurerClientErrorConfig.class)
    @Mapping("${root}:41310")
    public interface ConfigurerClientError {

        @ConfigureWith(ConfigurerClientSampleErrorConfig.class)
        String sample();
    }
}
