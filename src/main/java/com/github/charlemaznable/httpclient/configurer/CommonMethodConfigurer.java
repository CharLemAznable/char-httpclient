package com.github.charlemaznable.httpclient.configurer;

import com.google.common.base.Splitter;

import java.util.List;

public interface CommonMethodConfigurer extends
        CommonAbstractConfigurer, MappingConfigurer {

    @Override
    default List<String> urls() {
        return Splitter.on(",").omitEmptyStrings()
                .trimResults().splitToList(getString("path", ""));
    }
}
