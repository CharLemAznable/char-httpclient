package com.github.charlemaznable.httpclient.ohclient.configurer;

import com.github.charlemaznable.httpclient.configurer.CommonMethodConfigurer;

public interface OkHttpMethodConfigurer extends CommonMethodConfigurer, OkHttpAbstractConfigurer,
        ClientProxyDisabledConfigurer, ClientSSLDisabledConfigurer, ClientInterceptorsCleanupConfigurer {

    @Override
    default boolean disabledClientProxy() {
        return getBoolean("disabledClientProxy");
    }

    @Override
    default boolean disabledSSLSocketFactory() {
        return getBoolean("disabledSSLSocketFactory");
    }

    @Override
    default boolean disabledX509TrustManager() {
        return getBoolean("disabledX509TrustManager");
    }

    @Override
    default boolean disabledHostnameVerifier() {
        return getBoolean("disabledHostnameVerifier");
    }

    @Override
    default boolean cleanupInterceptors() {
        return getBoolean("cleanupInterceptors");
    }
}
