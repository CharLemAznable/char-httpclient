package com.github.charlemaznable.httpclient.ohclient.testclient;

import com.github.charlemaznable.miner.MinerConfig;
import com.github.charlemaznable.httpclient.common.Mapping.UrlProvider;

import java.lang.reflect.Method;

@MinerConfig("DEFAULT_DATA")
public interface TestSampleUrlProvider extends UrlProvider {

    @MinerConfig(defaultValue = "/sample")
    String sample();

    @Override
    default String url(Class<?> clazz, Method method) {
        return sample();
    }
}
