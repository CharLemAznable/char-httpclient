package com.github.charlemaznable.httpclient.logging;

import com.github.charlemaznable.httpclient.common.CommonExecute;
import com.github.charlemaznable.httpclient.wfclient.elf.RequestSpecConfigurer;
import com.google.auto.service.AutoService;
import org.slf4j.Logger;
import org.springframework.web.reactive.function.client.WebClient;

@AutoService(RequestSpecConfigurer.class)
public final class LoggingWfClientRequestSpecConfigurer implements RequestSpecConfigurer {

    @Override
    public void configRequestSpec(WebClient.RequestBodyUriSpec requestSpec,
                                  CommonExecute<?, ?, ?, ?> execute) {
        requestSpec.attribute(Logger.class.getName(), execute.executeMethod().defaultClass().logger());
    }
}
