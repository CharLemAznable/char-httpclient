package com.github.charlemaznable.httpclient.ohclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.core.lang.Objectt;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientInterceptorsConfigurer;
import okhttp3.Interceptor;

import java.util.List;

public interface ClientInterceptorsConfig extends ClientInterceptorsConfigurer {

    @Config("interceptors")
    String interceptorsString();

    @Override
    default List<Interceptor> interceptors() {
        return Objectt.parseObjects(interceptorsString(), Interceptor.class);
    }
}
