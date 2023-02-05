package com.github.charlemaznable.httpclient.configurer;

public interface RequestExtendDisabledConfigurer extends Configurer {

    default boolean disabledRequestExtend() {
        return true;
    }
}
