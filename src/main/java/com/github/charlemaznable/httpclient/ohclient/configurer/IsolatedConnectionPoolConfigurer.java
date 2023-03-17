package com.github.charlemaznable.httpclient.ohclient.configurer;

import com.github.charlemaznable.httpclient.configurer.Configurer;
import okhttp3.ConnectionPool;

public interface IsolatedConnectionPoolConfigurer extends Configurer {

    default boolean isolatedConnectionPool() {
        return true;
    }

    default ConnectionPool customConnectionPool() {
        return null;
    }
}
