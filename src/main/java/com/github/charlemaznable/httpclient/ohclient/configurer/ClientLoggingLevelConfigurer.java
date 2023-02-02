package com.github.charlemaznable.httpclient.ohclient.configurer;

import com.github.charlemaznable.httpclient.configurer.Configurer;
import okhttp3.logging.HttpLoggingInterceptor;

public interface ClientLoggingLevelConfigurer extends Configurer {

    HttpLoggingInterceptor.Level loggingLevel();
}
