package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.configurer.RequestExtendDisabledConfigurer;
import org.apache.commons.lang3.BooleanUtils;

public interface RequestExtendDisabledConfig extends RequestExtendDisabledConfigurer {

    @Config("disabledRequestExtend")
    String disabledRequestExtendString();

    @Override
    default boolean disabledRequestExtend() {
        return BooleanUtils.toBoolean(disabledRequestExtendString());
    }
}
