package com.github.charlemaznable.httpclient.configurer;

import com.google.common.base.Splitter;

import java.util.List;

import static com.github.charlemaznable.core.lang.Str.toStr;

public interface CommonMethodConfigurer extends
        CommonAbstractConfigurer, MappingConfigurer {

    String path();

    @Override
    default List<String> urls() {
        return Splitter.on(",").omitEmptyStrings()
                .trimResults().splitToList(toStr(path()));
    }
}
