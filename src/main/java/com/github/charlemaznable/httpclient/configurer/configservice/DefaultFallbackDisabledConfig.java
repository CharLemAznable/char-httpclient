package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.configurer.DefaultFallbackDisabledConfigurer;
import org.apache.commons.lang3.BooleanUtils;

public interface DefaultFallbackDisabledConfig extends DefaultFallbackDisabledConfigurer {

    @Config("disabledDefaultFallback")
    String disabledDefaultFallbackString();

    @Override
    default boolean disabledDefaultFallback() {
        return BooleanUtils.toBoolean(disabledDefaultFallbackString());
    }
}
