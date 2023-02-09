package com.github.charlemaznable.httpclient.configurer;

import com.github.charlemaznable.httpclient.common.FallbackFunction;
import com.github.charlemaznable.httpclient.common.HttpStatus;

import java.util.Map;

@SuppressWarnings("rawtypes")
public interface StatusFallbacksConfigurer extends Configurer {

    Map<HttpStatus, Class<? extends FallbackFunction>> statusFallbackMapping();
}
