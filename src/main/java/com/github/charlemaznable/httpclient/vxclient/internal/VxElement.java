package com.github.charlemaznable.httpclient.vxclient.internal;

import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.httpclient.common.CommonElement;
import com.github.charlemaznable.httpclient.vxclient.configurer.VertxWebClientBuilderConfigurer;
import com.github.charlemaznable.httpclient.vxclient.elf.WebClientBuilder;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.impl.WebClientBase;

import java.lang.reflect.AnnotatedElement;

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
        if (configurer() instanceof VertxWebClientBuilderConfigurer builderConfigurer) {
            return builderConfigurer.configBuilder(
                    new WebClientBuilder((WebClientBase) defaultClient)).build();
        }
        return defaultClient;
    }
}
