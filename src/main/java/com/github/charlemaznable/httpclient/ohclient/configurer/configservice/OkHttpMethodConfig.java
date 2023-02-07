package com.github.charlemaznable.httpclient.ohclient.configurer.configservice;

import com.github.charlemaznable.httpclient.configurer.configservice.CommonMethodConfig;

public interface OkHttpMethodConfig extends OkHttpAbstractConfig, CommonMethodConfig,
        ClientProxyDisabledConfig, ClientSSLDisabledConfig, ClientInterceptorsCleanupConfig {
}
