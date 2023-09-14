package com.github.charlemaznable.httpclient.configurer;

import com.github.charlemaznable.httpclient.common.ResilienceRecover;

public interface ResilienceFallbackConfigurer extends Configurer {

    default ResilienceRecover<?> recover() {
        return null;
    }
}
