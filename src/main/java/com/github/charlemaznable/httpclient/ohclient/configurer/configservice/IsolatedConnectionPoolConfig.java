package com.github.charlemaznable.httpclient.ohclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.ohclient.configurer.IsolatedConnectionPoolConfigurer;
import org.apache.commons.lang3.BooleanUtils;

public interface IsolatedConnectionPoolConfig extends IsolatedConnectionPoolConfigurer {

    @Config("isolatedConnectionPool")
    String isolatedConnectionPoolString();

    @Override
    default boolean isolatedConnectionPool() {
        return BooleanUtils.toBoolean(isolatedConnectionPoolString());
    }
}
