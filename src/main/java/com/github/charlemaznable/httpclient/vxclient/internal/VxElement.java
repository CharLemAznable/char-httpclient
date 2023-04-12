package com.github.charlemaznable.httpclient.vxclient.internal;

import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.httpclient.common.CommonElement;
import com.github.charlemaznable.httpclient.vxclient.configurer.VertxWebClientOptionsConfigurer;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.impl.WebClientBase;
import lombok.val;

import java.lang.reflect.AnnotatedElement;

import static java.util.Objects.requireNonNull;
import static org.joor.Reflect.on;

final class VxElement extends CommonElement<VxBase> {

    public VxElement(Vertx vertx, Factory factory) {
        super(new VxBase(vertx), factory);
    }

    @Override
    public void initialize(AnnotatedElement element, VxBase superBase) {
        super.initialize(element, superBase);
        base().client = buildClient(superBase.client);
    }

    private WebClient buildClient(WebClient defaultClient) {
        if (configurer() instanceof VertxWebClientOptionsConfigurer optionsConfigurer) {
            return WebClient.create(requireNonNull(base().vertx),
                    optionsConfigurer.configOptions(getWebClientOptions(defaultClient)));
        }
        return defaultClient;
    }

    private WebClientOptions getWebClientOptions(WebClient client) {
        val clientBase = (WebClientBase) client;
        return new WebClientOptions(on(clientBase).<WebClientOptions>get("options"));
    }
}
