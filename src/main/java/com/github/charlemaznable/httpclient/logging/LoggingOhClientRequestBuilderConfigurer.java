package com.github.charlemaznable.httpclient.logging;

import com.github.charlemaznable.httpclient.common.CommonExecute;
import com.github.charlemaznable.httpclient.ohclient.elf.RequestBuilderConfigurer;
import com.google.auto.service.AutoService;
import okhttp3.Request;
import org.slf4j.Logger;

@AutoService(RequestBuilderConfigurer.class)
public final class LoggingOhClientRequestBuilderConfigurer implements RequestBuilderConfigurer {

    @Override
    public void configRequestBuilder(Request.Builder requestBuilder, CommonExecute<?, ?, ?, ?> execute) {
        requestBuilder.tag(Logger.class, execute.executeMethod().defaultClass().logger());
    }
}
