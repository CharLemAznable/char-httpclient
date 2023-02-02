package com.github.charlemaznable.httpclient.ohclient.configurer;

import com.github.charlemaznable.httpclient.configurer.Configurer;
import okhttp3.Interceptor;

import java.util.List;

public interface ClientInterceptorsConfigurer extends Configurer {

    List<Interceptor> interceptors();
}
