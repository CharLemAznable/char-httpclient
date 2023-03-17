package com.github.charlemaznable.httpclient.ohclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.ohclient.configurer.IsolatedDispatcherConfigurer;
import org.apache.commons.lang3.BooleanUtils;

public interface IsolatedDispatcherConfig extends IsolatedDispatcherConfigurer {

    @Config("isolatedDispatcher")
    String isolatedDispatcherString();

    @Override
    default boolean isolatedDispatcher() {
        return BooleanUtils.toBoolean(isolatedDispatcherString());
    }
}
