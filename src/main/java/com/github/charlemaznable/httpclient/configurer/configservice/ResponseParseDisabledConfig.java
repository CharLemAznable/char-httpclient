package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.configurer.ResponseParseDisabledConfigurer;
import org.apache.commons.lang3.BooleanUtils;

public interface ResponseParseDisabledConfig extends ResponseParseDisabledConfigurer {

    @Config("disabledResponseParse")
    String disabledResponseParseString();

    @Override
    default boolean disabledResponseParse() {
        return BooleanUtils.toBoolean(disabledResponseParseString());
    }
}
