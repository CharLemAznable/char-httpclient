package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.configurer.ExtraUrlQueryDisabledConfigurer;
import org.apache.commons.lang3.BooleanUtils;

public interface ExtraUrlQueryDisabledConfig extends ExtraUrlQueryDisabledConfigurer {

    @Config("disabledExtraUrlQuery")
    String disabledExtraUrlQueryString();

    @Override
    default boolean disabledExtraUrlQuery() {
        return BooleanUtils.toBoolean(disabledExtraUrlQueryString());
    }
}
