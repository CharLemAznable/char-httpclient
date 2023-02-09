package com.github.charlemaznable.httpclient.ohclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientProxyConfigurer;
import lombok.val;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;

public interface ClientProxyConfig extends ClientProxyConfigurer {

    @Config("proxy")
    String proxyString();

    @Override
    default Proxy proxy() {
        return notNullThen(proxyString(), v -> {
            val uri = URI.create(v);
            return new Proxy(Proxy.Type.valueOf(uri.getScheme().toUpperCase()),
                    new InetSocketAddress(uri.getHost(), uri.getPort()));
        });
    }
}
