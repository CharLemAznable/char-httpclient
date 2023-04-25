package com.github.charlemaznable.httpclient.vxclient.internal;

import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.vxclient.configurer.VertxWebClientBuilderConfigurer;
import com.github.charlemaznable.httpclient.vxclient.elf.VertxReflectFactory;
import com.github.charlemaznable.httpclient.vxclient.elf.VxWebClient;
import com.github.charlemaznable.httpclient.vxclient.elf.VxWebClientBuilder;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.impl.WebClientBase;
import io.vertx.ext.web.client.impl.WebClientInternal;
import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VxElementTest {

    @Test
    public void testVxElement() {
        val vertx = Vertx.vertx();
        val vertxFactory = new VertxReflectFactory(vertx);
        val vxElement = new VxElement(vertx, vertxFactory);
        vxElement.initializeConfigurer(TestElementClass.class);

        val defaultBase = new VxBase(vertx);
        val options = new WebClientOptions();

        defaultBase.client = new WebClientBase(vertx.createHttpClient(options), options);
        vxElement.initialize(TestElementClass.class, defaultBase);
        assertTrue(vxElement.base().client instanceof VxWebClient);

        defaultBase.client = new IllegalWebClient(new WebClientBase(vertx.createHttpClient(options), options));
        assertThrows(IllegalArgumentException.class, () ->
                vxElement.initialize(TestElementClass.class, defaultBase));
    }

    @ConfigureWith(TestConfigurer.class)
    public static class TestElementClass {
    }

    public static class TestConfigurer implements VertxWebClientBuilderConfigurer {

        @Override
        public VxWebClientBuilder configBuilder(VxWebClientBuilder builder) {
            return builder;
        }
    }

    @AllArgsConstructor
    public static class IllegalWebClient implements WebClientInternal {

        @Delegate
        private WebClientBase webClientBase;
    }
}
