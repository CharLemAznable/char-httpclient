package com.github.charlemaznable.httpclient.ohclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientProxyDisabledConfigurer;
import org.apache.commons.lang3.BooleanUtils;

public interface ClientProxyDisabledConfig extends ClientProxyDisabledConfigurer {

    @Config("disabledClientProxy")
    String disabledClientProxyString();

    @Override
    default boolean disabledClientProxy() {
        return BooleanUtils.toBoolean(disabledClientProxyString());
    }
}
