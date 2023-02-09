package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import com.github.charlemaznable.httpclient.configurer.RequestMethodConfigurer;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;

public interface RequestMethodConfig extends RequestMethodConfigurer {

    @Config("requestMethod")
    String requestMethodString();

    @Override
    default HttpMethod requestMethod() {
        try {
            return notNullThen(requestMethodString(), HttpMethod::valueOf);
        } catch (Exception e) {
            return null;
        }
    }
}
