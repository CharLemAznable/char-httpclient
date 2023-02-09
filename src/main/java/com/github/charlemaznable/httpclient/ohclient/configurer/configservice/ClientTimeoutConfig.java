package com.github.charlemaznable.httpclient.ohclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientTimeoutConfigurer;
import org.apache.commons.lang3.math.NumberUtils;

import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_CALL_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_CONNECT_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_READ_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_WRITE_TIMEOUT;

public interface ClientTimeoutConfig extends ClientTimeoutConfigurer {

    @Config("callTimeout")
    String callTimeoutString();

    @Config("connectTimeout")
    String connectTimeoutString();

    @Config("readTimeout")
    String readTimeoutString();

    @Config("writeTimeout")
    String writeTimeoutString();

    @Override
    default long callTimeout() {
        return NumberUtils.toLong(callTimeoutString(), DEFAULT_CALL_TIMEOUT);
    }

    @Override
    default long connectTimeout() {
        return NumberUtils.toLong(connectTimeoutString(), DEFAULT_CONNECT_TIMEOUT);
    }

    @Override
    default long readTimeout() {
        return NumberUtils.toLong(readTimeoutString(), DEFAULT_READ_TIMEOUT);
    }

    @Override
    default long writeTimeout() {
        return NumberUtils.toLong(writeTimeoutString(), DEFAULT_WRITE_TIMEOUT);
    }
}
