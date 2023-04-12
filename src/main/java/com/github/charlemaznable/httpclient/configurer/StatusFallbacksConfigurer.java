package com.github.charlemaznable.httpclient.configurer;

import com.github.charlemaznable.httpclient.common.FallbackFunction;
import com.github.charlemaznable.httpclient.common.HttpStatus;

import java.util.Map;

public interface StatusFallbacksConfigurer extends Configurer {

    Map<HttpStatus, FallbackFunction<?>> statusFallbackMapping();
}
