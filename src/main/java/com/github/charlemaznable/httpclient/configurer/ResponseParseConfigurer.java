package com.github.charlemaznable.httpclient.configurer;

import com.github.charlemaznable.httpclient.common.ResponseParse;

public interface ResponseParseConfigurer extends Configurer {

    ResponseParse.ResponseParser responseParser();
}
