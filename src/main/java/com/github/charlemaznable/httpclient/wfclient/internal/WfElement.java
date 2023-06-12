package com.github.charlemaznable.httpclient.wfclient.internal;

import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.httpclient.common.CommonElement;
import com.github.charlemaznable.httpclient.wfclient.configurer.WebFluxClientBuilderConfigurer;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.AnnotatedElement;

final class WfElement extends CommonElement<WfBase> {

    public WfElement(Factory factory) {
        super(new WfBase(), factory);
    }

    @Override
    public void initialize(AnnotatedElement element, WfBase superBase) {
        super.initialize(element, superBase);
        base().client = buildClient(superBase.client);
    }

    private WebClient buildClient(WebClient defaultClient) {
        if (configurer() instanceof WebFluxClientBuilderConfigurer builderConfigurer) {
            return builderConfigurer.configBuilder(defaultClient.mutate()).build();
        }
        return defaultClient;
    }
}
