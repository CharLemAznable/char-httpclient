package com.github.charlemaznable.httpclient.configurer;

public interface ResponseParseDisabledConfigurer extends Configurer {

    default boolean disabledResponseParse() {
        return true;
    }
}
