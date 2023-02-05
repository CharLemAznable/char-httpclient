package com.github.charlemaznable.httpclient.configurer;

import com.google.common.base.Splitter;

import java.util.List;

public interface CommonMethodConfigurer extends CommonAbstractConfigurer,
        RequestExtendDisabledConfigurer, ResponseParseDisabledConfigurer, ExtraUrlQueryDisabledConfigurer {

    @Override
    default List<String> urls() {
        return Splitter.on(",").omitEmptyStrings()
                .trimResults().splitToList(getString("path", ""));
    }

    @Override
    default boolean disabledRequestExtend() {
        return getBoolean("disabledRequestExtend");
    }

    @Override
    default boolean disabledResponseParse() {
        return getBoolean("disabledResponseParse");
    }

    @Override
    default boolean disabledExtraUrlQuery() {
        return getBoolean("disabledExtraUrlQuery");
    }
}
