package com.github.charlemaznable.httpclient.ohclient.testclient;

import com.github.charlemaznable.configservice.diamond.DiamondConfig;
import com.github.charlemaznable.httpclient.common.Mapping.UrlProvider;

import java.lang.reflect.Method;

@DiamondConfig("DEFAULT_DATA")
public interface TestSampleUrlProvider extends UrlProvider {

    @DiamondConfig(defaultValue = "/sample")
    String sample();

    @Override
    default String url(Class<?> clazz, Method method) {
        return sample();
    }
}
