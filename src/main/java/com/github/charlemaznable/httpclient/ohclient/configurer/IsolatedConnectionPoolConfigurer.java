package com.github.charlemaznable.httpclient.ohclient.configurer;

import com.github.charlemaznable.httpclient.configurer.Configurer;

public interface IsolatedConnectionPoolConfigurer extends Configurer {

    default boolean isolatedConnectionPool() {
        return true;
    }
}
