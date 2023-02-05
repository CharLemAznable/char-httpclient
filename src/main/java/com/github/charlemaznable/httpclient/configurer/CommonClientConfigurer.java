package com.github.charlemaznable.httpclient.configurer;

import com.google.common.base.Splitter;

import java.util.List;

public interface CommonClientConfigurer extends CommonAbstractConfigurer,
        MappingMethodNameDisabledConfigurer, DefaultFallbackDisabledConfigurer {

    @Override
    default List<String> urls() {
        return Splitter.on(",").omitEmptyStrings()
                .trimResults().splitToList(getString("baseUrl", ""));
    }

    @Override
    default boolean disabledMappingMethodName() {
        return getBoolean("dontMappingMethodName");
    }

    @Override
    default boolean disabledDefaultFallback() {
        return getBoolean("ignoreDefaultFallback");
    }
}
