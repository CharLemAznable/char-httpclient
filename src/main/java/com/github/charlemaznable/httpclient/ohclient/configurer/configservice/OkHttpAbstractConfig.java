package com.github.charlemaznable.httpclient.ohclient.configurer.configservice;

import com.github.charlemaznable.httpclient.configurer.configservice.CommonAbstractConfig;

public interface OkHttpAbstractConfig extends CommonAbstractConfig,
        ClientProxyConfig, ClientSSLConfig, IsolatedConnectionPoolConfig,
        ClientTimeoutConfig, ClientInterceptorsConfig, ClientLoggingLevelConfig, ClientDispatcherConfig {
}
