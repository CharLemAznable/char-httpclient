package com.github.charlemaznable.httpclient.ohclient.configurer;

import com.github.charlemaznable.httpclient.configurer.Configurer;

import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_CALL_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_CONNECT_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_READ_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_WRITE_TIMEOUT;

public interface ClientTimeoutConfigurer extends Configurer {

    default long callTimeout() {
        return DEFAULT_CALL_TIMEOUT;
    }

    default long connectTimeout() {
        return DEFAULT_CONNECT_TIMEOUT;
    }

    default long readTimeout() {
        return DEFAULT_READ_TIMEOUT;
    }

    default long writeTimeout() {
        return DEFAULT_WRITE_TIMEOUT;
    }
}
