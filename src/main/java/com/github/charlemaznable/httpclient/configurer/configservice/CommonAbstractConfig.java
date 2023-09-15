package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.httpclient.configurer.InitializationConfigurer;

public interface CommonAbstractConfig extends InitializationConfigurer,
        MappingConfig, AcceptCharsetConfig, ContentFormatConfig, RequestMethodConfig,
        FixedHeadersConfig, FixedPathVarsConfig, FixedParametersConfig, FixedContextsConfig,
        StatusFallbacksConfig, StatusSeriesFallbacksConfig,
        RequestExtendConfig, ResponseParseConfig, ExtraUrlQueryConfig, MappingBalanceConfig {
}
