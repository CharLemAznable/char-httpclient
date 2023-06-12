package com.github.charlemaznable.httpclient.wfclient.configurer;

import com.github.charlemaznable.httpclient.configurer.Configurer;
import org.springframework.web.reactive.function.client.WebClient;

public interface WebFluxClientBuilderConfigurer extends Configurer {

    WebClient.Builder configBuilder(WebClient.Builder builder);
}
