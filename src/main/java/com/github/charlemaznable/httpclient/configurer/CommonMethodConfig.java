package com.github.charlemaznable.httpclient.configurer;

import com.github.charlemaznable.configservice.Config;
import org.apache.commons.lang3.BooleanUtils;

public interface CommonMethodConfig extends CommonAbstractConfig,
        RequestExtendDisabledConfigurer, ResponseParseDisabledConfigurer, ExtraUrlQueryDisabledConfigurer {

    @Config("disabledRequestExtend")
    String disabledRequestExtendString();

    @Config("disabledResponseParse")
    String disabledResponseParseString();

    @Config("disabledExtraUrlQuery")
    String disabledExtraUrlQueryString();

    @Override
    default boolean disabledRequestExtend() {
        return BooleanUtils.toBoolean(disabledRequestExtendString());
    }

    @Override
    default boolean disabledResponseParse() {
        return BooleanUtils.toBoolean(disabledResponseParseString());
    }

    @Override
    default boolean disabledExtraUrlQuery() {
        return BooleanUtils.toBoolean(disabledExtraUrlQueryString());
    }
}
