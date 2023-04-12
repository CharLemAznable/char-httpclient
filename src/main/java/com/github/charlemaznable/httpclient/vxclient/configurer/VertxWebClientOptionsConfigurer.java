package com.github.charlemaznable.httpclient.vxclient.configurer;

import com.github.charlemaznable.httpclient.configurer.Configurer;
import io.vertx.ext.web.client.WebClientOptions;

public interface VertxWebClientOptionsConfigurer extends Configurer {

    WebClientOptions configOptions(WebClientOptions options);
}
