package com.github.charlemaznable.httpclient.vxclient.internal;

import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.httpclient.common.CommonElement;
import com.github.charlemaznable.httpclient.vxclient.configurer.VertxWebClientBuilderConfigurer;
import com.github.charlemaznable.httpclient.vxclient.elf.VxWebClient;
import com.github.charlemaznable.httpclient.vxclient.elf.VxWebClientBuilder;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.impl.WebClientBase;
import io.vertx.ext.web.client.impl.WebClientInternal;

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

    private WebClientInternal buildClient(WebClientInternal defaultClient) {
        if (configurer() instanceof VertxWebClientBuilderConfigurer builderConfigurer) {
            if (defaultClient instanceof WebClientBase webClientBase) {
                return builderConfigurer.configBuilder(
                        new VxWebClientBuilder(webClientBase)).build();
            } else if (defaultClient instanceof VxWebClient vxWebClient) {
                return builderConfigurer.configBuilder(
                        new VxWebClientBuilder(vxWebClient)).build();
            } else {
                throw new IllegalArgumentException("Unsupported WebClient Type: " + defaultClient.getClass());
            }
        }
        return defaultClient;
    }
}
