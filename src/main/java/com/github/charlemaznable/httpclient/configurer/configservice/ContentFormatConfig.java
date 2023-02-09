package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.common.ContentFormat;
import com.github.charlemaznable.httpclient.configurer.ContentFormatConfigurer;

import java.util.Optional;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;

public interface ContentFormatConfig extends ContentFormatConfigurer {

    @Config("contentFormatter")
    String contentFormatterString();

    @Override
    default ContentFormat.ContentFormatter contentFormatter() {
        return notNullThen(contentFormatterString(), v -> Optional
                .ofNullable(ContentFormat.ContentType.resolve(v))
                .map(ContentFormat.ContentType::getContentFormatter).orElse(null));
    }
}
