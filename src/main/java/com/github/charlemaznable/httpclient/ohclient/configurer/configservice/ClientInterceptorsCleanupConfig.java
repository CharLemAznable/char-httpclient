package com.github.charlemaznable.httpclient.ohclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientInterceptorsCleanupConfigurer;
import org.apache.commons.lang3.BooleanUtils;

public interface ClientInterceptorsCleanupConfig extends ClientInterceptorsCleanupConfigurer {

    @Config("cleanupInterceptors")
    String cleanupInterceptorsString();

    @Override
    default boolean cleanupInterceptors() {
        return BooleanUtils.toBoolean(cleanupInterceptorsString());
    }
}
