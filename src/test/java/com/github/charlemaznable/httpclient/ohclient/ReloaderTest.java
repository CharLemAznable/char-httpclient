package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.common.CommonReloaderTest;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReloaderTest extends CommonReloaderTest {

    @Test
    public void testReloader() {
        startMockWebServer();

        UrlReloader.setBaseUrl("${root}:41270");
        SampleReloader.setSamplePath("/sample");

        val ohLoader = OhFactory.ohLoader(reflectFactory());

        val httpClient = ohLoader.getClient(ReloadableClient.class);

        val response1 = httpClient.sample();
        assertEquals("mock server 1", response1);

        UrlReloader.setBaseUrl("${root}:41280");
        val response2 = httpClient.sample();
        assertEquals("mock server 2", response2);

        SampleReloader.setSamplePath("/sample2");
        val response3 = httpClient.sample();
        assertEquals("mock server 3", response3);

        httpClient.reload();
        val response4 = httpClient.sample();
        assertEquals("mock server 3", response4);

        shutdownMockWebServer();
    }

    @OhClient
    @ConfigureWith(UrlReloader.class)
    public interface ReloadableClient extends Reloadable {

        @ConfigureWith(SampleReloader.class)
        String sample();
    }
}
