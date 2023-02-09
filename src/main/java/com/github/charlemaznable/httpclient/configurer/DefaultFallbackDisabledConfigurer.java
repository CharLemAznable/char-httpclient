package com.github.charlemaznable.httpclient.configurer;

public interface DefaultFallbackDisabledConfigurer extends Configurer {

    default boolean disabledDefaultFallback() {
        return true;
    }
}
