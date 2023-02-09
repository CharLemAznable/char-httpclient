package com.github.charlemaznable.httpclient.ohclient.configurer;

import com.github.charlemaznable.httpclient.configurer.Configurer;

public interface ClientProxyDisabledConfigurer extends Configurer {

    default boolean disabledClientProxy() {
        return true;
    }
}
