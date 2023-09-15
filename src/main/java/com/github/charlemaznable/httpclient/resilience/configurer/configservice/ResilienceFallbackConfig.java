package com.github.charlemaznable.httpclient.resilience.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.core.lang.Objectt;
import com.github.charlemaznable.httpclient.resilience.configurer.ResilienceFallbackConfigurer;
import com.github.charlemaznable.httpclient.resilience.function.ResilienceRecover;

public interface ResilienceFallbackConfig extends ResilienceFallbackConfigurer {

    @Config("recover")
    String recoverString();

    @SuppressWarnings("unchecked")
    @Override
    default <T> ResilienceRecover<T> recover() {
        return Objectt.parseObject(recoverString(), ResilienceRecover.class);
    }
}
