package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.core.lang.Objectt;
import com.github.charlemaznable.httpclient.common.ResilienceRecover;
import com.github.charlemaznable.httpclient.configurer.ResilienceFallbackConfigurer;

public interface ResilienceFallbackConfig extends ResilienceFallbackConfigurer {

    @Config("recover")
    String recoverString();

    @Override
    default ResilienceRecover<?> recover() {
        return Objectt.parseObject(recoverString(), ResilienceRecover.class);
    }
}
