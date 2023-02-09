package com.github.charlemaznable.httpclient.configurer;

public interface MappingMethodNameDisabledConfigurer extends Configurer {

    default boolean disabledMappingMethodName() {
        return true;
    }
}
