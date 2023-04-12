package com.github.charlemaznable.httpclient.configurer;

import com.github.charlemaznable.httpclient.annotation.ResponseParse;

public interface ResponseParseConfigurer extends Configurer {

    ResponseParse.ResponseParser responseParser();
}
