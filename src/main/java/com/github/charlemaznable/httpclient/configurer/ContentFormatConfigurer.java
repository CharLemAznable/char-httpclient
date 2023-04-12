package com.github.charlemaznable.httpclient.configurer;

import com.github.charlemaznable.httpclient.annotation.ContentFormat;

public interface ContentFormatConfigurer extends Configurer {

    ContentFormat.ContentFormatter contentFormatter();
}
