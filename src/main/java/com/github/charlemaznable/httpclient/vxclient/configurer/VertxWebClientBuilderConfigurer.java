package com.github.charlemaznable.httpclient.vxclient.configurer;

import com.github.charlemaznable.httpclient.configurer.Configurer;
import com.github.charlemaznable.httpclient.vxclient.elf.VxWebClientBuilder;

public interface VertxWebClientBuilderConfigurer extends Configurer {

    VxWebClientBuilder configBuilder(VxWebClientBuilder builder);
}
