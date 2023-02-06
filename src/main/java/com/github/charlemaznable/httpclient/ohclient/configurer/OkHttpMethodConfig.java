package com.github.charlemaznable.httpclient.ohclient.configurer;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.configurer.CommonMethodConfig;
import org.apache.commons.lang3.BooleanUtils;

public interface OkHttpMethodConfig extends OkHttpAbstractConfig, CommonMethodConfig,
        ClientProxyDisabledConfigurer, ClientSSLDisabledConfigurer, ClientInterceptorsCleanupConfigurer {

    @Config("disabledClientProxy")
    String disabledClientProxyString();

    @Config("disabledSSLSocketFactory")
    String disabledSSLSocketFactoryString();

    @Config("disabledX509TrustManager")
    String disabledX509TrustManagerString();

    @Config("disabledHostnameVerifier")
    String disabledHostnameVerifierString();

    @Config("cleanupInterceptors")
    String cleanupInterceptorsString();

    @Override
    default boolean disabledClientProxy() {
        return BooleanUtils.toBoolean(disabledClientProxyString());
    }

    @Override
    default boolean disabledSSLSocketFactory() {
        return BooleanUtils.toBoolean(disabledSSLSocketFactoryString());
    }

    @Override
    default boolean disabledX509TrustManager() {
        return BooleanUtils.toBoolean(disabledX509TrustManagerString());
    }

    @Override
    default boolean disabledHostnameVerifier() {
        return BooleanUtils.toBoolean(disabledHostnameVerifierString());
    }

    @Override
    default boolean cleanupInterceptors() {
        return BooleanUtils.toBoolean(cleanupInterceptorsString());
    }
}
