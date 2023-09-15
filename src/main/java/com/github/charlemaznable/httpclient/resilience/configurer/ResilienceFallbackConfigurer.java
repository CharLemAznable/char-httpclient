package com.github.charlemaznable.httpclient.resilience.configurer;

import com.github.charlemaznable.httpclient.configurer.Configurer;
import com.github.charlemaznable.httpclient.resilience.function.ResilienceRecover;

public interface ResilienceFallbackConfigurer extends Configurer {

    default <T> ResilienceRecover<T> recover() {
        return null;
    }
}
