package com.github.charlemaznable.httpclient.ohclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientSSLDisabledConfigurer;
import org.apache.commons.lang3.BooleanUtils;

public interface ClientSSLDisabledConfig extends ClientSSLDisabledConfigurer {

    @Config("disabledSSLSocketFactory")
    String disabledSSLSocketFactoryString();

    @Config("disabledX509TrustManager")
    String disabledX509TrustManagerString();

    @Config("disabledHostnameVerifier")
    String disabledHostnameVerifierString();

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
}
