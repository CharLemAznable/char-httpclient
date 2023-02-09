package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.configurer.MappingMethodNameDisabledConfigurer;
import org.apache.commons.lang3.BooleanUtils;

public interface MappingMethodNameDisabledConfig extends MappingMethodNameDisabledConfigurer {

    @Config("disabledMappingMethodName")
    String disabledMappingMethodNameString();

    @Override
    default boolean disabledMappingMethodName() {
        return BooleanUtils.toBoolean(disabledMappingMethodNameString());
    }
}
