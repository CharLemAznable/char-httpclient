package com.github.charlemaznable.httpclient.configurer;

import com.google.common.base.Splitter;

import java.util.List;

import static com.github.charlemaznable.core.lang.Str.toStr;

public interface CommonClientConfigurer extends
        CommonAbstractConfigurer, MappingConfigurer,
        MappingMethodNameDisabledConfigurer, DefaultFallbackDisabledConfigurer {

    String baseUrl();

    @Override
    default List<String> urls() {
        return Splitter.on(",").omitEmptyStrings()
                .trimResults().splitToList(toStr(baseUrl()));
    }

    boolean dontMappingMethodName();

    @Override
    default boolean disabledMappingMethodName() {
        return dontMappingMethodName();
    }

    boolean ignoreDefaultFallback();

    @Override
    default boolean disabledDefaultFallback() {
        return ignoreDefaultFallback();
    }
}
