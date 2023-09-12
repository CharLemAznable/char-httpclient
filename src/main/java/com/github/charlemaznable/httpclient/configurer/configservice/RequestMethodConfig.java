package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import com.github.charlemaznable.httpclient.configurer.RequestMethodConfigurer;

import static com.github.charlemaznable.httpclient.configurer.configservice.ConfigurerElf.parseStringToValue;

public interface RequestMethodConfig extends RequestMethodConfigurer {

    @Config("requestMethod")
    String requestMethodString();

    @Override
    default HttpMethod requestMethod() {
        return parseStringToValue(requestMethodString(), null, HttpMethod::valueOf);
    }
}
