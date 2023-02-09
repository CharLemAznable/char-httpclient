package com.github.charlemaznable.httpclient.ohclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientLoggingLevelConfigurer;
import okhttp3.logging.HttpLoggingInterceptor;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;

public interface ClientLoggingLevelConfig extends ClientLoggingLevelConfigurer {

    @Config("loggingLevel")
    String loggingLevelString();

    @Override
    default HttpLoggingInterceptor.Level loggingLevel() {
        try {
            return notNullThen(loggingLevelString(), HttpLoggingInterceptor.Level::valueOf);
        } catch (Exception e) {
            return null;
        }
    }
}
