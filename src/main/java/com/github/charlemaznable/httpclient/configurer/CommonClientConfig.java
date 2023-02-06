package com.github.charlemaznable.httpclient.configurer;

import com.github.charlemaznable.configservice.Config;
import org.apache.commons.lang3.BooleanUtils;

public interface CommonClientConfig extends CommonAbstractConfig,
        MappingMethodNameDisabledConfigurer, DefaultFallbackDisabledConfigurer {

    @Config("disabledMappingMethodName")
    String disabledMappingMethodNameString();

    @Config("disabledDefaultFallback")
    String disabledDefaultFallbackString();

    @Override
    default boolean disabledMappingMethodName() {
        return BooleanUtils.toBoolean(disabledMappingMethodNameString());
    }

    @Override
    default boolean disabledDefaultFallback() {
        return BooleanUtils.toBoolean(disabledDefaultFallbackString());
    }
}
