package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.configurer.MappingConfigurer;
import com.google.common.base.Splitter;

import java.util.List;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;

public interface MappingConfig extends MappingConfigurer {

    @Config("urls")
    String urlsString();

    @Override
    default List<String> urls() {
        return notNullThen(urlsString(), v -> Splitter.on(",")
                .omitEmptyStrings().trimResults().splitToList(v));
    }
}
