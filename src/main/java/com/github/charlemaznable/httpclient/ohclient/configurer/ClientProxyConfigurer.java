package com.github.charlemaznable.httpclient.ohclient.configurer;

import com.github.charlemaznable.httpclient.configurer.Configurer;

import java.net.Proxy;

public interface ClientProxyConfigurer extends Configurer {

    Proxy proxy();
}
