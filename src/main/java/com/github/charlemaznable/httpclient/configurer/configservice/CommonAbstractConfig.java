package com.github.charlemaznable.httpclient.configurer.configservice;

public interface CommonAbstractConfig extends MappingConfig,
        AcceptCharsetConfig, ContentFormatConfig, RequestMethodConfig,
        FixedHeadersConfig, FixedPathVarsConfig, FixedParametersConfig, FixedContextsConfig,
        StatusFallbacksConfig, StatusSeriesFallbacksConfig,
        RequestExtendConfig, ResponseParseConfig, ExtraUrlQueryConfig, MappingBalanceConfig {
}
