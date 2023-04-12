package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.core.lang.Objectt;
import com.github.charlemaznable.httpclient.annotation.ResponseParse;
import com.github.charlemaznable.httpclient.configurer.ResponseParseConfigurer;

public interface ResponseParseConfig extends ResponseParseConfigurer {

    @Config("responseParser")
    String responseParserString();

    @Override
    default ResponseParse.ResponseParser responseParser() {
        return Objectt.parseObject(responseParserString(), ResponseParse.ResponseParser.class);
    }
}
