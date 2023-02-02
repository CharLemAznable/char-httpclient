package com.github.charlemaznable.httpclient.configurer;

import com.github.charlemaznable.httpclient.common.HttpMethod;

public interface RequestMethodConfigurer extends Configurer {

    HttpMethod requestMethod();
}
