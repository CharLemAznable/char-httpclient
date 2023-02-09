package com.github.charlemaznable.httpclient.ohclient.configurer;

import com.github.charlemaznable.httpclient.configurer.Configurer;

public interface ClientInterceptorsCleanupConfigurer extends Configurer {

    default boolean cleanupInterceptors() {
        return true;
    }
}
