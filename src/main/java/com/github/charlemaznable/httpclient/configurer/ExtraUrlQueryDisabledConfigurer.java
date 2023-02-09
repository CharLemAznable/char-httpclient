package com.github.charlemaznable.httpclient.configurer;

public interface ExtraUrlQueryDisabledConfigurer extends Configurer {

    default boolean disabledExtraUrlQuery() {
        return true;
    }
}
