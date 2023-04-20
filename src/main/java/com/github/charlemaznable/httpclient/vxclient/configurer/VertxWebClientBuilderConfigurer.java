package com.github.charlemaznable.httpclient.vxclient.configurer;

import com.github.charlemaznable.httpclient.configurer.Configurer;
import com.github.charlemaznable.httpclient.vxclient.elf.WebClientBuilder;

public interface VertxWebClientBuilderConfigurer extends Configurer {

    WebClientBuilder configBuilder(WebClientBuilder builder);
}
