package com.github.charlemaznable.httpclient.ohclient.configurer;

import com.github.charlemaznable.httpclient.configurer.Configurer;

public interface ClientSSLDisabledConfigurer extends Configurer {

    default boolean disabledSSLSocketFactory() {
        return true;
    }

    default boolean disabledX509TrustManager() {
        return true;
    }

    default boolean disabledHostnameVerifier() {
        return true;
    }
}
