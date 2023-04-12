package com.github.charlemaznable.httpclient.ohclient.internal;

import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.httpclient.common.CommonElement;
import com.github.charlemaznable.httpclient.ohclient.configurer.OkHttpClientBuilderConfigurer;
import okhttp3.OkHttpClient;

import java.lang.reflect.AnnotatedElement;

final class OhElement extends CommonElement<OhBase> {

    public OhElement(Factory factory) {
        super(new OhBase(), factory);
    }

    @Override
    public void initialize(AnnotatedElement element, OhBase superBase) {
        super.initialize(element, superBase);
        base().client = buildClient(superBase.client);
    }

    private OkHttpClient buildClient(OkHttpClient defaultClient) {
        if (configurer() instanceof OkHttpClientBuilderConfigurer builderConfigurer) {
            return builderConfigurer.configBuilder(defaultClient.newBuilder()).build();
        }
        return defaultClient;
    }
}
