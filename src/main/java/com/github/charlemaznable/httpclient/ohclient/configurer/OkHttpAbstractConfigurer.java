package com.github.charlemaznable.httpclient.ohclient.configurer;

import com.github.charlemaznable.httpclient.configurer.CommonAbstractConfigurer;
import okhttp3.logging.HttpLoggingInterceptor;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_CALL_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_CONNECT_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_READ_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_WRITE_TIMEOUT;

public interface OkHttpAbstractConfigurer extends CommonAbstractConfigurer,
        ClientProxyConfigurer, ClientSSLConfigurer, IsolatedConnectionPoolConfigurer,
        ClientTimeoutConfigurer, ClientInterceptorsConfigurer, ClientLoggingLevelConfigurer {

    @Override
    default boolean isolatedConnectionPool() {
        return getBoolean("isolatedConnectionPool");
    }

    @Override
    default long callTimeout() {
        return getLong("callTimeout", DEFAULT_CALL_TIMEOUT);
    }

    @Override
    default long connectTimeout() {
        return getLong("connectTimeout", DEFAULT_CONNECT_TIMEOUT);
    }

    @Override
    default long readTimeout() {
        return getLong("readTimeout", DEFAULT_READ_TIMEOUT);
    }

    @Override
    default long writeTimeout() {
        return getLong("writeTimeout", DEFAULT_WRITE_TIMEOUT);
    }

    @Override
    default HttpLoggingInterceptor.Level loggingLevel() {
        try {
            return notNullThen(getString("loggingLevel"), HttpLoggingInterceptor.Level::valueOf);
        } catch (Exception e) {
            return null;
        }
    }
}
