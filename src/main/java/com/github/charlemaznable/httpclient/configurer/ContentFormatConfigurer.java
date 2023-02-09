package com.github.charlemaznable.httpclient.configurer;

import com.github.charlemaznable.httpclient.common.ContentFormat;

public interface ContentFormatConfigurer extends Configurer {

    ContentFormat.ContentFormatter contentFormatter();
}
