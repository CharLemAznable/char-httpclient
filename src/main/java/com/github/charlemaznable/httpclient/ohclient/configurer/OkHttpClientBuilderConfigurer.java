package com.github.charlemaznable.httpclient.ohclient.configurer;

import com.github.charlemaznable.httpclient.configurer.Configurer;
import okhttp3.OkHttpClient;

public interface OkHttpClientBuilderConfigurer extends Configurer {

    OkHttpClient.Builder configBuilder(OkHttpClient.Builder builder);
}
